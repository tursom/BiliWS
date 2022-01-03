package cn.tursom

import cn.tursom.core.notifyAll
import cn.tursom.core.toJson
import cn.tursom.core.toPrettyJson
import cn.tursom.core.wait
import cn.tursom.ws.BiliWSClient
import cn.tursom.ws.CmdEnum
import kotlinx.coroutines.runBlocking
import org.junit.Test

class WSConnectTest {
  @Test
  fun testConnect(): Unit = runBlocking {
    val biliWSClient = BiliWSClient(8584389, onClose = {
      notifyAll {
      }
    })
//    biliWSClient.addGiftListener {
//      println(it)
//    }
    biliWSClient.addDanmuListener {
      println(it)
    }

    biliWSClient.connect()
    biliWSClient.wait {
    }
  }

}
