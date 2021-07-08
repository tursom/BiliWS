package cn.tursom.ws.danmu

import cn.tursom.core.UncheckedCast
import cn.tursom.core.cast
import cn.tursom.danmu.Record

@OptIn(UncheckedCast::class)
data class DanmuBrandInfo(
  val level: Int,
  val sigh: String,
  val anchor: String,
  val roomId: Int,
) {
  fun toProtobuf(): Record.DanmuBrandInfo = Record.DanmuBrandInfo.newBuilder()
    .setLevel(level)
    .setSing(sigh)
    .setAnchor(anchor)
    .setRoomId(roomId)
    .build()

  companion object {
    fun parse(brand: List<Any>): DanmuBrandInfo? {
      return if (brand.isEmpty()) null
      else DanmuBrandInfo(
        level = brand[0].cast(),
        sigh = brand[1].cast(),
        anchor = brand[2].cast(),
        roomId = brand[3].cast()
      )
    }
  }
}