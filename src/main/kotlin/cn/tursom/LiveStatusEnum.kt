package cn.tursom

import cn.tursom.exception.LiveException

enum class LiveStatusEnum {
  /**
   * 直播
   */
  LIVING,

  /**
   * 轮播视频
   */
  LOOPING,

  /**
   * 啥也没有播
   */
  NOTHING
  ;

  companion object {
    fun valueOf(status: Int) = when (status) {
      0 -> LiveStatusEnum.NOTHING
      1 -> LiveStatusEnum.LIVING
      2 -> LiveStatusEnum.LOOPING
      else -> throw LiveException("无法获取直播状态")
    }
  }
}