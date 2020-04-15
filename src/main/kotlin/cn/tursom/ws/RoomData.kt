package cn.tursom.ws

data class RoomData(
  val encrypted: Boolean,
  val hidden_till: Int,
  val is_hidden: Boolean,
  val is_locked: Boolean,
  val is_portrait: Boolean,
  val is_sp: Int,
  val live_status: Int,
  val live_time: Long,
  val lock_till: Int,
  val need_p2p: Int,
  val pwd_verified: Boolean,
  val room_id: Int,
  val room_shield: Int,
  val short_id: Int,
  val special_type: Int,
  val uid: Int
)