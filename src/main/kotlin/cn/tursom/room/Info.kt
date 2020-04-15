package cn.tursom.room

data class Info(
  val face: String,
  val gender: Int,
  val identification: Int,
  val mobile_verify: Int,
  val official_verify: OfficialVerify,
  val platform_user_level: Int,
  val rank: String,
  val uid: Int,
  val uname: String,
  val vip_type: Int
)