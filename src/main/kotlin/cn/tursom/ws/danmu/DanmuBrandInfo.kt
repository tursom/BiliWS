package cn.tursom.ws.danmu

import cn.tursom.core.cast

data class DanmuBrandInfo(
  val level: Int,
  val sigh: String,
  val anchor: String,
  val roomId: Int
) {
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