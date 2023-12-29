package cn.tursom

import cn.tursom.core.util.notifyAll
import cn.tursom.core.util.toJson
import cn.tursom.core.util.wait
import cn.tursom.ws.BiliWSClient
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.File

class WSConnectTest {
  @Test
  fun testConnect(): Unit = runBlocking {
    val biliWSClient = BiliWSClient(664, onClose = {
      notifyAll {
      }
    })
//    biliWSClient.addGiftListener {
//      println(it)
//    }
    val outputStream = File("cmd_log_${System.currentTimeMillis()}.json").outputStream()
    biliWSClient.addCmdListener {
      outputStream.write((it.toJson() + "\n").toByteArray())
      outputStream.flush()
    }
    biliWSClient.addDanmuListener {
      println(it)
    }

    biliWSClient.connect()
    biliWSClient.wait {
    }
  }

}
