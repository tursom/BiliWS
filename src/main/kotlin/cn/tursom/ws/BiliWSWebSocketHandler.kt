package cn.tursom.ws

import cn.tursom.core.*
import cn.tursom.core.buffer.ByteBuffer
import cn.tursom.core.buffer.impl.HeapByteBuffer
import cn.tursom.core.coroutine.GlobalScope
import cn.tursom.core.datastruct.concurrent.ConcurrentLinkedList
import cn.tursom.core.ws.SimpWebSocketClient
import cn.tursom.core.ws.SimpWebSocketHandler
import cn.tursom.room.WsServerData
import cn.tursom.ws.danmu.DanmuInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class BiliWSWebSocketHandler(
  private val biliWSClient: BiliWSClient,
  private val roomInit: RoomInit,
  private val serverConf: WsServerData,
  private val roomId: Int,
  private val connectLock: AtomicBoolean,
  private val livingListenerMap: ConcurrentLinkedList<() -> Unit>,
  private val danmuListenerMap: ConcurrentLinkedList<(DanmuInfo) -> Unit>,
  private val cmdListenerMap: ConcurrentHashMap<String, ConcurrentLinkedList<BiliWSClient.(Map<String, Any>) -> Unit>>,
  private val codeListenerMap: ConcurrentHashMap<Int, ConcurrentLinkedList<BiliWSClient.(ByteArray) -> Unit>>,
) : SimpWebSocketHandler {
  private var future: Job? = null
  override fun onOpen(client: SimpWebSocketClient<SimpWebSocketHandler>) {
    BiliWSClient.logger.debug("WebSocketClient onOpen")
    val conn =
      """{"uid": 0,"roomid": ${roomInit.data.room_id},"protover": 2,"platform": "web","clientver": "1.12.0","type": 2,"key":"${serverConf.token}"}"""
    BiliWSClient.logger.debug("msg: {}", +{ conn })
    val msg = conn.toByteArray()
    val data = HeapByteBuffer(16 + msg.size)
    BiliWSPackageHead(16 + msg.size, 16, 1, 7, 1).apply {
      BiliWSClient.logger.debug("packet header: {}, {}", this, +{ toByteArray().toHexString() })
    }.writeTo(data)
    data.put(msg)
    client.write(data)
    future = GlobalScope.launch {
      while (true) {
        delay(30.seconds().toMillis())
        try {
          val buffer = HeapByteBuffer(31)
          BiliWSPackageHead(31, 16, code = 2, sequence = 1, version = 1).writeTo(buffer)
          buffer.put("[object Object]")
          client.write(buffer)
          BiliWSClient.logger.debug("BiliWSClient send heart beat, room id: {}", roomId)
        } catch (e: Throwable) {
          BiliWSClient.logger.error("heart beat send exception: {}", e)
          throw e
        }
      }
    }
    biliWSClient.onOpen(biliWSClient)
  }

  override fun onClose(client: SimpWebSocketClient<SimpWebSocketHandler>) {
    BiliWSClient.logger.info("WebSocketClient onClose")
    connectLock.set(false)
    if (biliWSClient.autoReconnect) GlobalScope.launch {
      BiliWSClient.reconnect(biliWSClient)
    }
    try {
      future?.cancel()
    } catch (_: Throwable) {
    }
    biliWSClient.onClose(biliWSClient)
  }

  override fun readMessage(client: SimpWebSocketClient<SimpWebSocketHandler>, msg: ByteBuffer) {
    try {
      while (msg.readable != 0) {
        val head = BiliWSPackageHead().readFrom(msg)
        BiliWSClient.logger.debug("WebSocketClient onMessage: {}", msg)
        BiliWSClient.logger.debug("WebSocketClient onMessage header: {}", head)
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
          1 -> {
            if (head.version.toInt() == 1 && msg.readable - (head.totalSize - head.headSize) > 0) {
              msg.getBytes(15)
            }
          }
          0 -> handleMessage(head, bytes)
        }
      }
    } catch (e: Exception) {
      msg.readPosition = 0
      val bytes = msg.getBytes()
      BiliWSClient.log.error("exception bytes: ", bytes.toHexString(), e)
    }
  }

  fun handleMessage(head: BiliWSPackageHead, bytes: ByteArray) {
    BiliWSClient.logger.debug("WebSocketClient onMessage header: {}", head)
    BiliWSClient.logger.trace(
      "WebSocketClient onMessage msg:\n|- hex: {}\n|- UTF-8: {}",
      +{ bytes.toHexString() },
      +{ bytes.toUTF8String() }
    )
    BiliWSClient.logger.debug(
      "WebSocketClient onMessage msg: {}",
      +{ bytes.toUTF8String() }
    )
    GlobalScope.launch {
      @Suppress("NON_EXHAUSTIVE_WHEN")
      when (head.code) {
        BiliLiveWSCode.SEND_MSG_REPLY.code -> {
          BiliWSClient.logger.debug("SEND_MSG_REPLY: {}", BiliLiveWSCode.SEND_MSG_REPLY)
          val data = BiliWSClient.gson.fromJson<Map<String, Any>>(bytes.toUTF8String())
          val cmd = data["cmd"].uncheckedCast<String>()
          when (cmd) {
            "LIVE" -> livingListenerMap.forEach { action ->
              try {
                action()
              } catch (e: Throwable) {
                BiliWSClient.logger.error("an exception caused on handle start live", e)
              }
            }
            "DANMU_MSG" -> {
              val danmu = DanmuInfo.parse(data["info"].uncheckedCast())
              danmuListenerMap.forEach { action ->
                try {
                  action(danmu)
                } catch (e: Throwable) {
                  BiliWSClient.logger.error("an exception caused on handle danmu", e)
                }
              }
            }
          }
          listOfNotNull(cmdListenerMap[cmd], cmdListenerMap[CmdEnum.ALL.value]).forEach {
            it.forEach { action ->
              try {
                biliWSClient.action(data)
              } catch (e: Throwable) {
                BiliWSClient.logger.error("an exception caused on handle cmd: {}", cmd, e)
              }
            }
          }
        }
        BiliLiveWSCode.HEARTBEAT_REPLY.code -> BiliWSClient.logger.debug(
          "heart beat response from server: {}",
          HeapByteBuffer(bytes).getInt()
        )
        else -> BiliWSClient.logger.warn("unsupported code: {}", head.code)
      }
      codeListenerMap[head.code]?.forEach {
        try {
          biliWSClient.it(bytes)
        } catch (e: Throwable) {
          BiliWSClient.logger.error("an exception caused on handle code {}", head.code, e)
        }
      }
    }
  }

  override fun onError(client: SimpWebSocketClient<SimpWebSocketHandler>, e: Throwable) {
    BiliWSClient.logger.error("WebSocketClient onError", e)
    client.close()
  }
}