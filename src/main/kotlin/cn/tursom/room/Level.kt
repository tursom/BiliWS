package cn.tursom.room

data class Level(
  val anchor_score: Int,
  val color: Int,
  val cost: Int,
  val master_level: MasterLevel,
  val rcost: Long,
  val svip: Int,
  val svip_time: String,
  val uid: Int,
  val update_time: String,
  val user_level: Int,
  val user_score: String,
  val vip: Int,
  val vip_time: String
)