package cn.tursom.ws.danmu

import cn.tursom.danmu.Record

enum class NavigationEnum(val code: Int, val protobufValue: Record.NavigationEnum) {
  NONE(0, Record.NavigationEnum.NONE),

  /**
   * 总督
   */
  GOVERNOR(1, Record.NavigationEnum.GOVERNOR),

  /**
   * 提督
   */
  ADMIRAL(2, Record.NavigationEnum.ADMIRAL),

  /**
   * 舰长
   */
  CAPTAIN(3, Record.NavigationEnum.CAPTAIN);

  companion object {
    fun valueOf(code: Int): NavigationEnum = when (code) {
      0 -> NONE
      1 -> GOVERNOR
      2 -> ADMIRAL
      3 -> CAPTAIN
      else -> throw IndexOutOfBoundsException()
    }

    fun fromProtobuf(navigationEnum: Record.NavigationEnum) = when (navigationEnum) {
      Record.NavigationEnum.NONE -> NONE
      Record.NavigationEnum.GOVERNOR -> GOVERNOR
      Record.NavigationEnum.ADMIRAL -> ADMIRAL
      Record.NavigationEnum.CAPTAIN -> CAPTAIN
      Record.NavigationEnum.UNRECOGNIZED -> NONE
    }
  }
}