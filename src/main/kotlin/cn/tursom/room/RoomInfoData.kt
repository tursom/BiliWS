package cn.tursom.room

data class RoomInfoData(
  val allow_change_area_time: Int,
  val allow_upload_cover_time: Int,
  val area_id: Int,
  val area_name: String,
  val area_pendants: String,
  val attention: Int,
  val background: String,
  val battle_id: Int,
  val description: String,
  val hot_words: List<String>,
  val hot_words_status: Int,
  val is_anchor: Int,
  val is_portrait: Boolean,
  val is_strict_room: Boolean,
  val keyframe: String,
  val live_status: Int,
  val live_time: String,
  val new_pendants: NewPendants,
  val old_area_id: Int,
  val online: Int,
  val parent_area_id: Int,
  val parent_area_name: String,
  val pendants: String,
  val pk_id: Int,
  val pk_status: Int,
  val room_id: Int,
  val room_silent_level: Int,
  val room_silent_second: Int,
  val room_silent_type: String,
  val short_id: Int,
  val studio_info: StudioInfo,
  val tags: String,
  val title: String,
  val uid: Int,
  val up_session: String,
  val user_cover: String,
  val verify: String
) {
  fun getUrlId() = if (short_id != 0) {
    short_id
  } else {
    room_id
  }
}
