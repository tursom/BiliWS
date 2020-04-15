package cn.tursom.ws.danmu

import cn.tursom.core.cast

data class DanmuUserInfo(
  val uid: Int,
  val nickName: String,
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
  val svip: Boolean
) {
  companion object {
    fun parse(userInfo: List<Any>): DanmuUserInfo {
      return DanmuUserInfo(
        uid = userInfo[0].cast(),
        nickName = userInfo[1].cast(),
        admin = userInfo[2].cast<Number>().toInt() != 0,
        vip = userInfo[3].cast<Number>().toInt() != 0,
        svip = userInfo[4].cast<Number>().toInt() != 0
      )
    }
  }
}