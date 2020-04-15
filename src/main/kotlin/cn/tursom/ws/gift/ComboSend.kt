package cn.tursom.ws.gift

data class ComboSend(
    val action: String,
    val combo_id: String,
    val combo_num: Int,
    val gift_id: Int,
    val gift_name: String,
    val gift_num: Int,
    val uid: Int,
    val uname: String
)