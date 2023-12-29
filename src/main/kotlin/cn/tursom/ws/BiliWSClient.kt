package cn.tursom.ws

import cn.tursom.Listener
import cn.tursom.LiveStatusEnum
import cn.tursom.RoomUtils
import cn.tursom.core.coroutine.GlobalScope
import cn.tursom.core.datastruct.concurrent.ConcurrentLinkedList
import cn.tursom.core.delegation.filter
import cn.tursom.core.delegation.locked
import cn.tursom.core.delegation.observer.Listenable
import cn.tursom.core.delegation.observer.listenable
import cn.tursom.core.util.fromJson
import cn.tursom.core.util.minutes
import cn.tursom.core.reflect.Parser
import cn.tursom.core.util.uncheckedCast
import cn.tursom.core.ws.SimpWebSocketClient
import cn.tursom.core.ws.SimpWebSocketHandler
import cn.tursom.http.client.AsyncHttpRequest
import cn.tursom.log.impl.Slf4jImpl
import cn.tursom.room.RoomInfoData
import cn.tursom.storage.LiveTime
import cn.tursom.ws.danmu.DanmuInfo
import cn.tursom.ws.gift.Gift
import com.google.gson.GsonBuilder
import io.netty.util.AttributeMap
import io.netty.util.DefaultAttributeMap
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.Closeable
import java.lang.ref.SoftReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.set

@Suppress("unused", "MemberVisibilityCanBePrivate")
class BiliWSClient(
  val roomId: Int,
  internal val onOpen: BiliWSClient.() -> Unit = {},
  private val liveTime: LiveTime? = null,
  internal val onClose: BiliWSClient.() -> Unit = {},
) : Closeable, AttributeMap by DefaultAttributeMap() {
  var autoReconnect: Boolean = true
  private val connection: Boolean get() = client?.closed ?: false
  var roomInfo: RoomInfoData = runBlocking { RoomUtils.getRoomInfo(roomId) }
    private set
  private var client: SimpWebSocketClient<SimpWebSocketHandler>? = null
  private val livingListenerMap = ConcurrentLinkedList<() -> Unit>()
  private val danmuListenerMap = ConcurrentLinkedList<(DanmuInfo) -> Unit>()
  private val cmdListenerMap =
    ConcurrentHashMap<String, ConcurrentLinkedList<BiliWSClient.(Map<String, Any>) -> Unit>>()
  private val codeListenerMap = ConcurrentHashMap<Int, ConcurrentLinkedList<BiliWSClient.(ByteArray) -> Unit>>()
  private var reconnectJob: Job? = null
  private var connectLock = AtomicBoolean()

  val userInfo = runBlocking { RoomUtils.getLiveUserInfo(roomInfo.room_id) }

  @Suppress("UNNECESSARY_SAFE_CALL", "USELESS_ELVIS")
  val userName: String
    get() = userInfo.info?.uname ?: roomId.toString()


  @OptIn(Listenable::class)
  var living: Boolean by listenable(LiveStatusEnum.valueOf(roomInfo.live_status) == LiveStatusEnum.LIVING)
    .filter { old, new -> old != new }
    .locked
    private set

  @Volatile
  private var liveStart: Long = liveTime?.getLiveTime(roomId) ?: if (living) System.currentTimeMillis() else 0
  val liveStartTime get() = liveStart

  init {
    addCmdListener(CmdEnum.LIVE) {
      living = true
      roomInfo = runBlocking { RoomUtils.getRoomInfo(roomId) }
      liveStart = System.currentTimeMillis()
      liveTime?.roomOnLive(roomId)
    }
    addCmdListener(CmdEnum.PREPARING) {
      living = false
    }
  }

  suspend fun connect(onOpen: BiliWSClient.() -> Unit = this.onOpen) {
    if (!connectLock.compareAndSet(false, true)) {
      return
    }
    try {
      val roomInit = AsyncHttpRequest.getStr("https://api.live.bilibili.com/room/v1/Room/room_init",
        mapOf("id" to roomId.toString())).fromJson<RoomInit>()

      logger.debug("room init: {}", roomInit)
      client?.close()
      val serverConf = RoomUtils.getLiveServerConf(roomInfo.room_id).data
      val wsServer = serverConf.host_server_list.first { it.wss_port != null || it.ws_port != null }
      val client = SimpWebSocketClient("ws${if (wsServer.wss_port != null) "s" else ""}://${
        wsServer.host
      }:${wsServer.wss_port}/sub".apply {
        logger.debug("connect to $this")
      }, BiliWSWebSocketHandler(this, roomInit, serverConf, roomId, connectLock,
        livingListenerMap, danmuListenerMap, cmdListenerMap, codeListenerMap))
      this.client = client
      client.open()
    } catch (e: Exception) {
      connectLock.set(false)
      throw e
    }
  }

  fun addLivingListener(action: () -> Unit): Listener {
    val iterator = livingListenerMap.addAndGetIterator(action)
    return ListIteratorListener(iterator)
  }

  fun addDanmuListener(action: (DanmuInfo) -> Unit): Listener {
    val iterator = danmuListenerMap.addAndGetIterator(action)
    return ListIteratorListener(iterator)
  }

  fun addCmdListener(msg: String = "", action: BiliWSClient.(Map<String, Any>) -> Unit): Listener {
    return cmdListenerMap.addToMap(msg, action)
  }

  fun addCmdListener(msg: CmdEnum, action: BiliWSClient.(Map<String, Any>) -> Unit): Listener {
    return addCmdListener(msg.value, action)
  }

  fun addGiftListener(action: BiliWSClient.(Gift) -> Unit): Listener {
    return addCmdListener(CmdEnum.SEND_GIFT) {
      action(Parser.parse(it["data"]!!, Gift::class.java)!!)
    }
  }

  inline fun <reified T : Any> addTypedCmdListener(
    msg: String,
    vararg valuePath: String,
    crossinline action: BiliWSClient.(T) -> Unit,
  ): Listener {
    return addCmdListener(msg) {
      var data: Any = it
      valuePath.forEach { key ->
        data = data.uncheckedCast<Map<String, Any>>()[key] ?: return@addCmdListener
      }
      this.action(Parser.parse(data, T::class.java)!!)
    }
  }

  inline fun <reified T : Any> addTypedCmdListener(
    msg: CmdEnum,
    vararg valuePath: String,
    crossinline action: BiliWSClient.(T) -> Unit,
  ): Listener = addTypedCmdListener(msg.value, *valuePath, action = action)

  inline fun <reified T : Any> addTypedDataCmdListener(
    msg: String,
    crossinline action: BiliWSClient.(T) -> Unit,
  ): Listener = addTypedCmdListener(msg, "data", action = action)

  inline fun <reified T : Any> addTypedDataCmdListener(
    msg: CmdEnum,
    crossinline action: BiliWSClient.(T) -> Unit,
  ): Listener = addTypedDataCmdListener(msg.value, action)

  fun addCodeListener(code: BiliLiveWSCode, action: BiliWSClient.(ByteArray) -> Unit) =
    addCodeListener(code.code, action)

  fun addCodeListener(code: Int, action: BiliWSClient.(ByteArray) -> Unit): Listener {
    return codeListenerMap.addToMap(code, action)
  }

  private fun <T, V> MutableMap<T, ConcurrentLinkedList<V>>.addToMap(key: T, value: V): ListIteratorListener {
    var list = this[key]
    if (list == null) synchronized(this) {
      list = this[key]
      if (list == null) {
        list = ConcurrentLinkedList()
        this[key] = list.uncheckedCast()
      }
    }
    val iterator = list!!.addAndGetIterator(value)
    return ListIteratorListener(iterator)
  }

  private class ListIteratorListener(val iterator: MutableListIterator<*>) : Listener {
    override var cancelled: Boolean = false
    override fun cancel() {
      if (!cancelled) synchronized(this) {
        iterator.remove()
        cancelled = true
      }
    }
  }

  override fun close() {
    reconnectJob?.cancel()
    client?.close()
  }

  companion object : Slf4jImpl() {
    val gson = GsonBuilder().registerTypeAdapterFactory(DataTypeAdaptor.FACTORY).create()

    suspend fun reconnect(biliWSClient: BiliWSClient) {
      while (!biliWSClient.connection) {
        biliWSClient.connect()
        delay(1.minutes().toMillis())
      }
    }

    private fun listen(biliWSClient: BiliWSClient) = GlobalScope.launch {
      val reference = SoftReference(biliWSClient)
      var connectionCheckFailedTimes = 0
      while (true) {
        delay(1.minutes().toMillis())
        val wsClient = reference.get() ?: return@launch
        connectionCheckFailedTimes++
        if (wsClient.connection) {
          connectionCheckFailedTimes = 0
        } else if (connectionCheckFailedTimes >= 4) {
          reconnect(wsClient)
        }
      }
    }

    private fun Int.isPower2(): Boolean {
      var n = when {
        this > 0 -> this
        this == 0 -> return true
        else -> -this // this < 0
      }
      while (n != 1) {
        if (n and 1 == 1) {
          return false
        }
        n = n shr 1
      }
      return true
    }
  }
}
