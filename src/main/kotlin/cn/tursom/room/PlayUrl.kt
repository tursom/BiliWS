package cn.tursom.room

data class PlayUrl(
  val code: Int,
  val `data`: PlayUrlData,
  val message: String,
  val ttl: Int
)