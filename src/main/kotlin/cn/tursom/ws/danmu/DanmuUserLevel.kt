package cn.tursom.ws.danmu

import cn.tursom.core.UncheckedCast
import cn.tursom.core.cast
import cn.tursom.danmu.Record

@OptIn(UncheckedCast::class)
data class DanmuUserLevel(
  val level: Int,
  val ranking: String,
) {
  fun toProtobuf(): Record.DanmuUserLevel = Record.DanmuUserLevel.newBuilder()
    .setLevel(level)
    .setRanking(ranking)
    .build()

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

    fun fromProtobuf(userLevel: Record.DanmuUserLevel) = DanmuUserLevel(
      userLevel.level,
      userLevel.ranking
    )
  }
}