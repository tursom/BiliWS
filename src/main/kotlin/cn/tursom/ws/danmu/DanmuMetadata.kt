package cn.tursom.ws.danmu

import cn.tursom.core.UncheckedCast
import cn.tursom.core.cast
import cn.tursom.danmu.Record

@OptIn(UncheckedCast::class)
data class DanmuMetadata(
  val time: Long,
  val rhythmStorm: Boolean,
) {
  fun toProtobuf(): Record.DanmuMetadata = Record.DanmuMetadata.newBuilder()
    .setTime(time)
    .setRhythmStorm(rhythmStorm)
    .build()

  companion object {
    fun parse(metadata: List<Any>): DanmuMetadata {
      return DanmuMetadata(
        time = metadata[4].cast(),
        rhythmStorm = metadata[5].cast<Number>().toInt() == 0
      )
    }

    fun fromProtobuf(metadata: Record.DanmuMetadata) = DanmuMetadata(
      metadata.time,
      metadata.rhythmStorm
    )
  }
}