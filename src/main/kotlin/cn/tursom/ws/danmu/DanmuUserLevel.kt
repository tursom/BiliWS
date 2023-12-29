package cn.tursom.ws.danmu

import cn.tursom.core.util.UncheckedCast
import cn.tursom.core.util.cast
import cn.tursom.danmu.Danmu

@OptIn(UncheckedCast::class)
data class DanmuUserLevel(
  val level: Int,
  val ranking: String,
) {
  fun toProtobuf(): Danmu.DanmuUserLevel = Danmu.DanmuUserLevel.newBuilder()
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

    fun fromProtobuf(userLevel: Danmu.DanmuUserLevel) = DanmuUserLevel(
      userLevel.level,
      userLevel.ranking
    )
  }
}