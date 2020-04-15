package cn.tursom.ws.danmu

import cn.tursom.core.cast

data class DanmuUserLevel(
  val level: Int,
  val ranking: String
) {
  companion object {
    fun parse(level: List<Any>): DanmuUserLevel {
      return DanmuUserLevel(
        level = level[0].cast(),
        ranking = level[3].let {
          when (it) {
            is Number -> it.toInt()
            else -> it
          }.toString()
        }
      )
    }
  }
}