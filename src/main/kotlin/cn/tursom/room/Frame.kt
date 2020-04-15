package cn.tursom.room

data class Frame(
  val area: Int,
  val area_old: Int,
  val bg_color: String,
  val bg_pic: String,
  val desc: String,
  val name: String,
  val position: Int,
  val use_old_area: Boolean,
  val value: String
)