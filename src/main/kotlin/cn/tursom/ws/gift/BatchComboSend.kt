package cn.tursom.ws.gift

data class BatchComboSend(
    val action: String,
    val batch_combo_id: String,
    val batch_combo_num: Int,
    val gift_id: Int,
    val gift_name: String,
    val gift_num: Int,
    val uid: Int,
    val uname: String
)