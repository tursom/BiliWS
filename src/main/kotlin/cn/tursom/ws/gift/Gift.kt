package cn.tursom.ws.gift

import cn.tursom.core.util.toJson
import cn.tursom.danmu.Danmu

@Suppress("unused")
data class Gift(
  val action: String,
  val addFollow: Int,
  val batch_combo_id: String,
  val batch_combo_send: BatchComboSend?,
  val beatId: String,
  val biz_source: String,
  val broadcast_id: Int,
  val coin_type: String,
  val combo_send: ComboSend?,
  val combo_stay_time: Int,
  val combo_total_coin: Int,
  val crit_prob: Int,
  val demarcation: Int,
  val draw: Int,
  val effect: Int,
  val effect_block: Int,
  val eventNum: Int,
  val eventScore: Int,
  val face: String,
  val giftId: Int,
  val giftName: String,
  val giftType: Int,
  val gold: Int,
  val guard_level: Int,
  val is_first: Boolean,
  val medal: List<Any>?,
  val metadata: String?,
  val newMedal: Int,
  val newTitle: Int,
  val notice_msg: List<Any>?,
  val num: Int,
  val price: Int,
  val rcost: Int,
  val remain: Int,
  val rnd: String,
  val silver: Int,
  val smallTVCountFlag: Boolean,
  val smalltv_msg: List<Any>?,
  val `super`: Int,
  val super_batch_gift_num: Int,
  val super_gift_num: Int,
  val tag_image: String,
  val tid: String,
  val timestamp: Int,
  val title: String?,
  val top_list: List<Any>?,
  val total_coin: Int,
  val uid: Int,
  val uname: String,
  val medal_info: MedalInfo?,
) {
  fun toProto(): Danmu.Gift? {
    return Danmu.Gift.newBuilder()
      .setAction(action)
      .setAddFollow(addFollow)
      .setBatchComboId(batch_combo_id)
      .setBatchComboSend(batch_combo_send?.toProto() ?: Danmu.BatchComboSend.getDefaultInstance())
      .setBeatId(beatId)
      .setBizSource(biz_source)
      .setBroadcastId(broadcast_id)
      .setCoinType(coin_type)
      .setComboSend(combo_send?.toProto() ?: Danmu.ComboSend.getDefaultInstance())
      .setComboStayTime(combo_stay_time)
      .setComboTotalCoin(combo_total_coin)
      .setCritProb(crit_prob)
      .setDemarcation(demarcation)
      .setDraw(draw)
      .setEffect(effect)
      .setEffectBlock(effect_block)
      .setEventNum(eventNum)
      .setEventScore(eventScore)
      .setFace(face)
      .setGiftId(giftId)
      .setGiftName(giftName)
      .setGiftType(giftType)
      .setGold(gold)
      .setGuardLevel(guard_level)
      .setIsFirst(is_first)
      .addAllMedal(medal?.map { it.toJson() } ?: emptyList())
      .setMetadata(metadata ?: "")
      .setNewMedal(newMedal)
      .setNewTitle(newTitle)
      .addAllNoticeMsg(notice_msg?.map { it.toJson() } ?: emptyList())
      .setNum(num)
      .setPrice(price)
      .setRcost(rcost)
      .setRemain(remain)
      .setRnd(rnd)
      .setSilver(silver)
      .setSmallTVCountFlag(smallTVCountFlag)
      .addAllSmalltvMsg(smalltv_msg?.map { it.toJson() } ?: emptyList())
      .setSuper(`super`)
      .setSuperBatchGiftNum(super_batch_gift_num)
      .setSuperGiftNum(super_gift_num)
      .setTagImage(tag_image)
      .setTid(tid)
      .setTimestamp(timestamp)
      .setTitle(title ?: "")
      .addAllTopList(top_list?.map { it.toJson() } ?: emptyList())
      .setTotalCoin(total_coin)
      .setUid(uid)
      .setUname(uname)
      .setMedalInfo(medal_info?.toProto() ?: Danmu.MedalInfo.getDefaultInstance())
      .build()
  }

  companion object {
    fun fromProto(gift: Danmu.Gift) = Gift(
      gift.action,
      gift.addFollow,
      gift.batchComboId,
      BatchComboSend.fromProto(gift.batchComboSend),
      gift.beatId,
      gift.bizSource,
      gift.broadcastId,
      gift.coinType,
      ComboSend.fromProto(gift.comboSend),
      gift.comboStayTime,
      gift.comboTotalCoin,
      gift.critProb,
      gift.demarcation,
      gift.draw,
      gift.effect,
      gift.effectBlock,
      gift.eventNum,
      gift.eventScore,
      gift.face,
      gift.giftId,
      gift.giftName,
      gift.giftType,
      gift.gold,
      gift.guardLevel,
      gift.isFirst,
      gift.medalList,
      gift.metadata,
      gift.newMedal,
      gift.newTitle,
      gift.noticeMsgList,
      gift.num,
      gift.price,
      gift.rcost,
      gift.remain,
      gift.rnd,
      gift.silver,
      gift.smallTVCountFlag,
      gift.smalltvMsgList,
      gift.`super`,
      gift.superBatchGiftNum,
      gift.superGiftNum,
      gift.tagImage,
      gift.tid,
      gift.timestamp,
      gift.title,
      gift.topListList,
      gift.totalCoin,
      gift.uid,
      gift.uname,
      MedalInfo.fromProto(gift.medalInfo)
    )
  }
}