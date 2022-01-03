package cn.tursom.ws.gift

import cn.tursom.danmu.Danmu


data class MedalInfo(
  val anchor_roomid: Int,
  val anchor_uname: String,
  val guard_level: Int,
  val icon_id: Int,
  val is_lighted: Int,
  val medal_color: Int,
  val medal_color_border: Int,
  val medal_color_end: Int,
  val medal_color_start: Int,
  val medal_level: Int,
  val medal_name: String,
  val special: String,
  val target_id: Int,
) {
  fun toProto() = Danmu.MedalInfo.newBuilder()
    .setAnchorRoomId(anchor_roomid)
    .setAnchorUname(anchor_uname)
    .setGuardLevel(guard_level)
    .setIconId(icon_id)
    .setIsLighted(is_lighted)
    .setMedalColor(medal_color)
    .setMedalColorBorder(medal_color_border)
    .setMedalColorEnd(medal_color_end)
    .setMedalColorStart(medal_color_start)
    .setMedalLevel(medal_level)
    .setMedalName(medal_name)
    .setSpecial(special)
    .setTargetId(target_id)
    .build()

  companion object {
    fun fromProto(medalInfo: Danmu.MedalInfo) = MedalInfo(
      medalInfo.anchorRoomId,
      medalInfo.anchorUname,
      medalInfo.guardLevel,
      medalInfo.iconId,
      medalInfo.isLighted,
      medalInfo.medalColor,
      medalInfo.medalColorBorder,
      medalInfo.medalColorEnd,
      medalInfo.medalColorStart,
      medalInfo.medalLevel,
      medalInfo.medalName,
      medalInfo.special,
      medalInfo.targetId,
    )
  }
}