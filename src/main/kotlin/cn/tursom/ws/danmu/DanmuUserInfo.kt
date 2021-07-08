package cn.tursom.ws.danmu

import cn.tursom.core.cast
import cn.tursom.danmu.Record

data class DanmuUserInfo(
  val uid: Int,
  val nickname: String,
  /**
   * 房管
   */
  val admin: Boolean,
  /**
   * 老爷
   */
  val vip: Boolean,
  /**
   * 年费老爷
   */
  @Suppress("SpellCheckingInspection")
  val svip: Boolean,
) {
  fun toProtobuf(): Record.DanmuUserInfo = Record.DanmuUserInfo.newBuilder()
    .setUid(uid)
    .setNickname(nickname)
    .setAdmin(admin)
    .setVip(vip)
    .setSvip(svip)
    .build()

  companion object {
    fun parse(userInfo: List<Any>): DanmuUserInfo {
      return DanmuUserInfo(
        uid = userInfo[0].cast(),
        nickname = userInfo[1].cast(),
        admin = userInfo[2].cast<Number>().toInt() != 0,
        vip = userInfo[3].cast<Number>().toInt() != 0,
        svip = userInfo[4].cast<Number>().toInt() != 0
      )
    }
  }
}