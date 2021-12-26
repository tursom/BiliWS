package cn.tursom

import cn.tursom.core.notifyAll
import cn.tursom.core.wait
import  cn.tursom.ws.BiliWSClient
import kotlinx.coroutines.runBlocking
import org.junit.Test

class WSConnectTest {
  @Test
  fun testConnect(): Unit = runBlocking {
    val biliWSClient = BiliWSClient(367966, onClose = {
      notifyAll {
      }
    })
    biliWSClient.addDanmuListener {
      println(it)
    }

    biliWSClient.connect()
    biliWSClient.wait {
    }
  }

}
