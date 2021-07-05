package cn.tursom.ws.danmu

import cn.tursom.core.UncheckedCast
import cn.tursom.core.cast

@OptIn(UncheckedCast::class)
data class DanmuMetaData(
  val time: Long,
  val rhythmStorm: Boolean
) {
  companion object {
    fun parse(metaData: List<Any>): DanmuMetaData {
      return DanmuMetaData(
        time = metaData[4].cast(),
        rhythmStorm = metaData[5].cast<Number>().toInt() == 0
      )
    }
  }
}