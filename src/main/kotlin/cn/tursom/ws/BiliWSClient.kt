package cn.tursom.ws

import cn.tursom.*
import cn.tursom.core.*
import cn.tursom.core.buffer.ByteBuffer
import cn.tursom.core.buffer.impl.HeapByteBuffer
import cn.tursom.core.datastruct.concurrent.ConcurrentLinkedList
import cn.tursom.log.impl.Slf4jImpl
import cn.tursom.room.RoomInfoData
import cn.tursom.utils.fromJson
import cn.tursom.ws.danmu.DanmuInfo
import com.google.gson.GsonBuilder
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import javax.annotation.PostConstruct
import kotlin.collections.set


class BiliWSClient(
  val roomId: Int,
  private val onOpen: BiliWSClient.() -> Unit = {},
  private val onClose: BiliWSClient.() -> Unit = {}
) {
  @Volatile
  private var connection: Boolean = false
  private val connectionCheckFaildTimes = AtomicInteger()
  private var roomInfo: RoomInfoData = RoomUtils.getRoomInfo(roomId)
  private var client: WebSocketClient? = null
  private val livingListenerMap = ConcurrentLinkedList<() -> Unit>()
  private val danmuListenerMap = ConcurrentLinkedList<(DanmuInfo) -> Unit>()
  private val cmdListenerMap =
    ConcurrentHashMap<String, ConcurrentLinkedList<BiliWSClient.(Map<String, Any>) -> Unit>>()
  private val codeListenerMap = ConcurrentHashMap<Int, ConcurrentLinkedList<BiliWSClient.(ByteArray) -> Unit>>()

  val userInfo = RoomUtils.getLiveUserInfo(roomInfo.room_id)

  //val userInfo = Unit.clone<LiveUserData>()
  val userName: String get() = userInfo.info?.uname ?: roomId.toString()

  init {
    clientCollection.add(this)
    addCmdListener(CmdEnum.LIVE) {
      living = true
      roomInfo = RoomUtils.getRoomInfo(roomId)
    }
    addCmdListener(CmdEnum.PREPARING) {
      living = false
    }
  }

  @Volatile
  var living: Boolean = LiveStatusEnum.valueOf(roomInfo.live_status) == LiveStatusEnum.LIVING
    set(value) {
      if (Throwable().stackTrace[1].className == BiliWSClient::class.java.name) {
        field = value
      }
    }

  fun getRoomInfo() = roomInfo

  @PostConstruct
  fun connect(onOpen: BiliWSClient.() -> Unit = this.onOpen) {
    val roomInit = HttpRequest.doGet(
      "https://api.live.bilibili.com/room/v1/Room/room_init",
      mapOf("id" to roomId.toString())
    ).fromJson<RoomInit>()

    logger.debug("room init: {}", roomInit)
    client?.close()
    val serverConf = RoomUtils.getLiveServerConf().data
    val wsServer = serverConf.host_server_list.first { it.wss_port != null || it.ws_port != null }
    val client = WebSocketClient(
      "ws${if (wsServer.wss_port != null) "s" else ""}://${
      wsServer.host}:${wsServer.wss_port}/sub",
      object : WebSocketHandler {
        lateinit var future: Future<*>
        override fun onOpen(client: WebSocketClient) {
          connection = true
          logger.debug("WebSocketClient onOpen")
          val conn =
            """{"uid": 0,"roomid": ${roomInit.data.room_id},"protover": 2,"platform": "web","clientver": "1.10.6","type": 2,"key":"${serverConf.token}"}"""
          logger.debug("msg: {}", +{ conn })
          val msg = conn.toByteArray()
          val data = HeapByteBuffer(16 + msg.size)
          BiliWSPackageHead(16 + msg.size, 16, 1, 7, 1).writeTo(data)
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
          }, 30, 30, TimeUnit.SECONDS)
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
            connect()
          }, 5, TimeUnit.SECONDS)
        }

        override fun readMessage(client: WebSocketClient, msg: ByteBuffer): Unit = loop({ msg.readable != 0 }) {
          val head = BiliWSPackageHead().readFrom(msg)
          logger.debug("WebSocketClient onMessage: {}", msg)
          logger.debug("WebSocketClient onMessage header: {}", head)
          val bytes = msg.getBytes(head.totalSize - head.headSize)
          when (head.version.toInt()) {
            2 -> @Suppress("NAME_SHADOWING") {
              val msg = HeapByteBuffer(bytes.undeflate())
              loop({ msg.readable != 0 }) {
                val head = BiliWSPackageHead().readFrom(msg)
                val bytes = msg.getBytes(head.totalSize - head.headSize)
                handleMessage(head, bytes)
              }
            }
            0 -> handleMessage(head, bytes)
          }
        }

        fun handleMessage(head: BiliWSPackageHead, bytes: ByteArray) {
          logger.debug("WebSocketClient onMessage header: {}", head)
          logger.debug(
            "WebSocketClient onMessage msg:\n|- hex: {}\n|- UTF-8: {}\n|- undeflate: {}",
            +{ bytes.toHexString() },
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

  companion object : Slf4jImpl() {
    // 用来监视ws连接情况的集合
    private val clientCollection: MutableCollection<BiliWSClient> = LinkedList()
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
          clientCollection.forEach {
            if (it.connection) {
              it.connectionCheckFaildTimes.set(0)
            } else if (it.connectionCheckFaildTimes.incrementAndGet() > 3) {
              try {
                it.connect()
                it.connectionCheckFaildTimes.set(0)
              } catch (e: Exception) {
              }
            }
          }
        } catch (e: Throwable) {
          errLog.log(e)
          logger.error("heart beat send exception: {}", e)
          throw e
        }
      }, 1, 1, TimeUnit.MINUTES)
    }
  }
}
