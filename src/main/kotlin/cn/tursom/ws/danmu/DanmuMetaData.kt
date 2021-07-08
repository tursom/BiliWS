package cn.tursom.ws.danmu

import cn.tursom.core.cast
import cn.tursom.danmu.Record

data class DanmuMetaData(
  val time: Long,
  val rhythmStorm: Boolean
) {
  fun toProtobuf(): Record.DanmuMetaData = Record.DanmuMetaData.newBuilder()
    .setTime(time)
    .setRhythmStorm(rhythmStorm)
    .build()

  companion object {
    fun parse(metaData: List<Any>): DanmuMetaData {
      return DanmuMetaData(
        time = metaData[4].cast(),
        rhythmStorm = metaData[5].cast<Number>().toInt() == 0
      )
    }
  }
}