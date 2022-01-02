package cn.tursom.ws

@Suppress("unused")
enum class CmdEnum(val value: String) {
  ALL(""),

  // 活动旗帜更新 v2？
  ACTIVITY_BANNER_UPDATE_V2("ACTIVITY_BANNER_UPDATE_V2"),

  // 组合结束？
  COMBO_END("COMBO_END"),

  // 组合发送？
  COMBO_SEND("COMBO_SEND"),

  // 弹幕信息
  DANMU_MSG("DANMU_MSG"),

  // 进入效果？
  ENTRY_EFFECT("ENTRY_EFFECT"),

  // GUARD购买？
  GUARD_BUY("GUARD_BUY"),

  // GUARD抽奖开始？
  GUARD_LOTTERY_START("GUARD_LOTTERY_START"),

  // GUARD信息？
  GUARD_MSG("GUARD_MSG"),

  // 直播开始
  LIVE("LIVE"),

  // 房间块信息？
  ROOM_BLOCK_MSG("ROOM_BLOCK_MSG"),

  // 房间变更？
  ROOM_CHANGE("ROOM_CHANGE"),

  // 房间等级？
  ROOM_RANK("ROOM_RANK"),

  // 房间准确时间信息更新？
  ROOM_REAL_TIME_MESSAGE_UPDATE("ROOM_REAL_TIME_MESSAGE_UPDATE"),

  // 提示信息？
  NOTICE_MSG("NOTICE_MSG"),

  // 准备中？，下播
  PREPARING("PREPARING"),

  // 发送礼物
  SEND_GIFT("SEND_GIFT"),

  // 超级聊天消息？SC？
  SUPER_CHAT_MESSAGE("SUPER_CHAT_MESSAGE"),

  // 超级聊天消息日语？
  SUPER_CHAT_MESSAGE_JPN("SUPER_CHAT_MESSAGE_JPN"),

  // 用户吐司信息？
  USER_TOAST_MSG("USER_TOAST_MSG"),

  // 周星钟？
  WEEK_STAR_CLOCK("WEEK_STAR_CLOCK"),

  // 欢迎？
  WELCOME("WELCOME"),

  // 欢迎舰长？
  WELCOME_GUARD("WELCOME_GUARD"),

  // 希望瓶
  WISH_BOTTLE("WISH_BOTTLE"),

  // 开始抽奖
  RAFFLE_START("RAFFLE_START"),

  // 抽奖结束
  RAFFLE_END("RAFFLE_END"),

  ROOM_SKIN_MSG("ROOM_SKIN_MSG"),

  SPECIAL_GIFT("SPECIAL_GIFT"),

  ROOM_SILENT_ON("ROOM_SILENT_ON"),

  HOUR_RANK_AWARDS("HOUR_RANK_AWARDS"),

  ROOM_BOX_MASTER("ROOM_BOX_MASTER"),

  PK_BATTLE_ENTRANCE("PK_BATTLE_ENTRANCE"),

  SYS_GIFT("SYS_GIFT"),

  NEW_ANCHOR_REWARD("new_anchor_reward"),

  VOICE_JOIN_LIST("VOICE_JOIN_LIST"),

  GUARD_ACHIEVEMENT_ROOM("GUARD_ACHIEVEMENT_ROOM"),

  VOICE_JOIN_ROOM_COUNT_INFO("VOICE_JOIN_ROOM_COUNT_INFO"),


  ;

  companion object {
    val nameSet = values().map { it.name }.toSet()
  }
}