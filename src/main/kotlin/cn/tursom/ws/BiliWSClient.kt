package cn.tursom.ws

import cn.tursom.*
import cn.tursom.core.*
import cn.tursom.core.fromJson
import cn.tursom.core.buffer.ByteBuffer
import cn.tursom.core.buffer.impl.HeapByteBuffer
import cn.tursom.core.datastruct.concurrent.ConcurrentLinkedList
import cn.tursom.core.ws.WebSocketClient
import cn.tursom.core.ws.WebSocketHandler
import cn.tursom.log.impl.Slf4jImpl
import cn.tursom.room.RoomInfoData
import cn.tursom.storage.LiveTime
import cn.tursom.utils.AsyncHttpRequest
import cn.tursom.ws.danmu.DanmuInfo
import com.google.gson.GsonBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.Closeable
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import javax.annotation.PostConstruct
import kotlin.collections.set


class BiliWSClient(
  val roomId: Int,
  private val onOpen: BiliWSClient.() -> Unit = {},
  private val liveTime: LiveTime? = null,
  private val onClose: BiliWSClient.() -> Unit = {},
) : Closeable {
  @Volatile
  private var connection: Boolean = false
  private var roomInfo: RoomInfoData = runBlocking { RoomUtils.getRoomInfo(roomId) }
  private var client: WebSocketClient? = null
  private val livingListenerMap = ConcurrentLinkedList<() -> Unit>()
  private val danmuListenerMap = ConcurrentLinkedList<(DanmuInfo) -> Unit>()
  private val cmdListenerMap =
    ConcurrentHashMap<String, ConcurrentLinkedList<BiliWSClient.(Map<String, Any>) -> Unit>>()
  private val codeListenerMap = ConcurrentHashMap<Int, ConcurrentLinkedList<BiliWSClient.(ByteArray) -> Unit>>()


  val userInfo = runBlocking { RoomUtils.getLiveUserInfo(roomInfo.room_id) }

  //val userInfo = Unit.clone<LiveUserData>()
  @Suppress("UNNECESSARY_SAFE_CALL", "USELESS_ELVIS")
  val userName: String
    get() = userInfo.info?.uname ?: roomId.toString()

  @Volatile
  var living: Boolean = LiveStatusEnum.valueOf(roomInfo.live_status) == LiveStatusEnum.LIVING
    set(value) {
      if (Throwable().stackTrace[1].className == BiliWSClient::class.java.name) {
        field = value
      }
    }

  @Volatile
  private var liveStart: Long = liveTime?.getLiveTime(roomId) ?: if (living) System.currentTimeMillis() else 0
  val liveStartTime get() = liveStart

  init {
    ShutdownHook.addHook {
      close()
    }
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

  fun getRoomInfo() = roomInfo

  @PostConstruct
  suspend fun connect(onOpen: BiliWSClient.() -> Unit = this.onOpen) {
    if (clientCollection.firstOrNull { it.first == this } == null) {
      clientCollection.add(this to AtomicInteger())
    }

    val roomInit = AsyncHttpRequest.getStr(
      "https://api.live.bilibili.com/room/v1/Room/room_init",
      mapOf("id" to roomId.toString())
    ).fromJson<RoomInit>()

    logger.debug("room init: {}", roomInit)
    client?.close()
    val serverConf = RoomUtils.getLiveServerConf(roomInfo.room_id).data
    val wsServer = serverConf.host_server_list.first { it.wss_port != null || it.ws_port != null }
    val client = WebSocketClient(
      "ws${if (wsServer.wss_port != null) "s" else ""}://${
        wsServer.host
      }:${wsServer.wss_port}/sub".apply {
        logger.debug("connect to $this")
      },
      object : WebSocketHandler {
        lateinit var future: Future<*>
        override fun onOpen(client: WebSocketClient) {
          connection = true
          logger.debug("WebSocketClient onOpen")
          val conn =
            """{"uid": 0,"roomid": ${roomInit.data.room_id},"protover": 2,"platform": "web","clientver": "1.12.0","type": 2,"key":"${serverConf.token}"}"""
          logger.debug("msg: {}", +{ conn })
          val msg = conn.toByteArray()
          val data = HeapByteBuffer(16 + msg.size)
          BiliWSPackageHead(16 + msg.size, 16, 1, 7, 1).apply {
            logger.debug("packet header: {}, {}", this, +{ toByteArray().toHexString() })
          }.writeTo(data)
          data.put(msg)
          //logger.debug("buffer: {}", data.array.toHexString())
          client.write(data)
          future = threadPool.scheduleAtFixedRate({
            try {
              val buffer = HeapByteBuffer(31)
              BiliWSPackageHead(31, 16, code = 2, sequence = 1, version = 1).writeTo(buffer)
              buffer.put("[object Object]")
              client.write(buffer)
              logger.debug("BiliWSClient send heart beat, room id: {}", roomId)
            } catch (e: Throwable) {
              errLog.log(e)
              logger.error("heart beat send exception: {}", e)
              throw e
            }
          }, 0, 30, TimeUnit.SECONDS)
          this@BiliWSClient.onOpen()
        }

        override fun onClose(client: WebSocketClient) {
          connection = false
          logger.info("WebSocketClient onClose")
          try {
            future.cancel(true)
          } catch (e: Throwable) {
          }
          onClose()
          threadPool.schedule({
            GlobalScope.launch {
              connect()
            }
          }, 5, TimeUnit.SECONDS)
        }

        override fun readMessage(client: WebSocketClient, msg: ByteBuffer) {
          while (msg.readable != 0) {
            val head = BiliWSPackageHead().readFrom(msg)
            logger.debug("WebSocketClient onMessage: {}", msg)
            logger.debug("WebSocketClient onMessage header: {}", head)
            val bytes = msg.getBytes(head.totalSize - head.headSize)
            when (head.version.toInt()) {
              2 -> @Suppress("NAME_SHADOWING") {
                val msg = HeapByteBuffer(bytes.undeflate())
                while (msg.readable != 0) {
                  val head = BiliWSPackageHead().readFrom(msg)
                  val bytes = msg.getBytes(head.totalSize - head.headSize)
                  handleMessage(head, bytes)
                }
              }
              0 -> handleMessage(head, bytes)
            }
          }
        }

        fun handleMessage(head: BiliWSPackageHead, bytes: ByteArray) {
          logger.debug("WebSocketClient onMessage header: {}", head)
          logger.trace(
            "WebSocketClient onMessage msg:\n|- hex: {}\n|- UTF-8: {}",
            +{ bytes.toHexString() },
            +{ bytes.toUTF8String() }
          )
          logger.debug(
            "WebSocketClient onMessage msg: {}",
            +{ bytes.toUTF8String() }
          )
          threadPool.execute {
            @Suppress("NON_EXHAUSTIVE_WHEN")
            when (head.code) {
              BiliLiveWSCode.SEND_MSG_REPLY.code -> {
                logger.debug("SEND_MSG_REPLY: {}", BiliLiveWSCode.SEND_MSG_REPLY)
                val data = gson.fromJson<Map<String, Any>>(bytes.toUTF8String())
                val cmd = data["cmd"]?.cast<String>()!!
                when (cmd) {
                  "LIVE" -> livingListenerMap.forEach { action ->
                    try {
                      action()
                    } catch (e: Throwable) {
                      errLog.log(e)
                      logger.error("an exception caused on handle start live", e)
                    }
                  }
                  "DANMU_MSG" -> {
                    val danmu = DanmuInfo.parse(data["info"]?.cast()!!)
                    danmuListenerMap.forEach { action ->
                      try {
                        action(danmu)
                      } catch (e: Throwable) {
                        errLog.log(e)
                        logger.error("an exception caused on handle danmu", e)
                      }
                    }
                  }
                }
                listOfNotNull(cmdListenerMap[cmd], cmdListenerMap[CmdEnum.ALL.value]).forEach {
                  it.forEach { action ->
                    try {
                      this@BiliWSClient.action(data)
                    } catch (e: Throwable) {
                      errLog.log(e)
                      logger.error("an exception caused on handle cmd: {}", cmd, e)
                    }
                  }
                }
              }
              BiliLiveWSCode.HEARTBEAT_REPLY.code -> logger.debug(
                "heart beat response from server: {}",
                HeapByteBuffer(bytes).getInt()
              )
              else -> logger.warn("unsupported code: {}", head.code)
            }
            codeListenerMap[head.code]?.forEach {
              try {
                this@BiliWSClient.it(bytes)
              } catch (e: Throwable) {
                errLog.log(e)
                logger.error("an exception caused on handle code {}", head.code, e)
              }
            }
          }
        }

        override fun onError(client: WebSocketClient, e: Throwable) {
          errLog.log(e)
          logger.error("WebSocketClient onError", e)
          client.close()
        }
      })
    this.client = client
    client.open()
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

  inline fun <reified T : Any> addTypedCmdListener(
    msg: String,
    vararg valuePath: String,
    crossinline action: BiliWSClient.(T) -> Unit,
  ): Listener {
    return addCmdListener(msg) {
      var data: Any = it
      valuePath.forEach { key ->
        data = data.cast<Map<String, Any>>()[key] ?: return@addCmdListener
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
        this[key] = list.cast()
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
    clientCollection.removeAll { it.first == this }
    client?.close()
  }

  companion object : Slf4jImpl() {
    // 用来监视ws连接情况的集合
    private val clientCollection: MutableCollection<Pair<BiliWSClient, AtomicInteger>> = ConcurrentLinkedQueue()
    val gson = GsonBuilder()
      .registerTypeAdapterFactory(DataTypeAdaptor.FACTORY)
      .create()
    private val threadNumber = AtomicInteger(0)
    private val threadPool = ScheduledThreadPoolExecutor(
      Runtime.getRuntime().availableProcessors(),
      ThreadFactory { Thread(it, "BiliWSClientWorker-${threadNumber.incrementAndGet()}") }
    ).apply {
      scheduleAtFixedRate({
        try {
          clientCollection.forEach { (it, connectionCheckFaildTimes) ->
            try {
              if (connectionCheckFaildTimes.incrementAndGet() == 8 || connectionCheckFaildTimes.get().isPower2()) {
                if (it.connection) {
                  connectionCheckFaildTimes.set(0)
                  return@forEach
                }
                GlobalScope.launch {
                  it.connect()
                }
              }
            } catch (e: Exception) {
            }
          }
        } catch (e: Throwable) {
          errLog.log(e)
          logger.error("reconnect exception: {}", e)
          throw e
        }
      }, 0, 1, TimeUnit.MINUTES)
    }

    private fun Int.isPower2(): Boolean {
      var n = when {
        this > 0 -> this
        this == 0 -> return true
        this < 0 -> -this
        else -> return false
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
