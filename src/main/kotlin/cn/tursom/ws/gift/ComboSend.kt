package cn.tursom.ws.gift

import cn.tursom.danmu.Danmu

data class ComboSend(
  val action: String,
  val combo_id: String,
  val combo_num: Int,
  val gift_id: Int,
  val gift_name: String,
  val gift_num: Int,
  val uid: Int,
  val uname: String,
) {
  fun toProto(): Danmu.ComboSend? {
    return Danmu.ComboSend.newBuilder()
      .setAction(action)
      .setComboId(combo_id)
      .setComboNum(combo_num)
      .setGiftId(gift_id)
      .setGiftName(gift_name)
      .setGiftNum(gift_num)
      .setUid(uid)
      .setUname(uname)
      .build()
  }

  companion object {
    fun fromProto(comboSend: Danmu.ComboSend) = ComboSend(
      comboSend.action,
      comboSend.comboId,
      comboSend.comboNum,
      comboSend.giftId,
      comboSend.giftName,
      comboSend.giftNum,
      comboSend.uid,
      comboSend.uname
    )
  }
}