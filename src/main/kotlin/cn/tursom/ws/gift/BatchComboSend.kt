package cn.tursom.ws.gift

import cn.tursom.danmu.Danmu

data class BatchComboSend(
  val action: String,
  val batch_combo_id: String,
  val batch_combo_num: Int,
  val gift_id: Int,
  val gift_name: String,
  val gift_num: Int,
  val uid: Int,
  val uname: String,
) {
  fun toProto(): Danmu.BatchComboSend? {
    return Danmu.BatchComboSend.newBuilder()
      .setAction(action)
      .setBatchComboId(batch_combo_id)
      .setBatchComboNum(batch_combo_num)
      .setGiftId(gift_id)
      .setGiftName(gift_name)
      .setGiftNum(gift_num)
      .setUid(uid)
      .setUname(uname)
      .build()
  }

  companion object {
    fun fromProto(batchComboSend: Danmu.BatchComboSend) = BatchComboSend(
      batchComboSend.action,
      batchComboSend.batchComboId,
      batchComboSend.batchComboNum,
      batchComboSend.giftId,
      batchComboSend.giftName,
      batchComboSend.giftNum,
      batchComboSend.uid,
      batchComboSend.uname
    )
  }
}