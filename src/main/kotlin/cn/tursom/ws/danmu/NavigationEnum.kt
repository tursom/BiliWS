package cn.tursom.ws.danmu

import cn.tursom.danmu.Danmu

enum class NavigationEnum(val code: Int, val protobufValue: Danmu.NavigationEnum) {
  NONE(0, Danmu.NavigationEnum.NONE),

  /**
   * 总督
   */
  GOVERNOR(1, Danmu.NavigationEnum.GOVERNOR),

  /**
   * 提督
   */
  ADMIRAL(2, Danmu.NavigationEnum.ADMIRAL),

  /**
   * 舰长
   */
  CAPTAIN(3, Danmu.NavigationEnum.CAPTAIN);

  companion object {
    fun valueOf(code: Int): NavigationEnum = when (code) {
      0 -> NONE
      1 -> GOVERNOR
      2 -> ADMIRAL
      3 -> CAPTAIN
      else -> throw IndexOutOfBoundsException()
    }

    fun fromProtobuf(navigationEnum: Danmu.NavigationEnum) = when (navigationEnum) {
      Danmu.NavigationEnum.NONE -> NONE
      Danmu.NavigationEnum.GOVERNOR -> GOVERNOR
      Danmu.NavigationEnum.ADMIRAL -> ADMIRAL
      Danmu.NavigationEnum.CAPTAIN -> CAPTAIN
      Danmu.NavigationEnum.UNRECOGNIZED -> NONE
    }
  }
}