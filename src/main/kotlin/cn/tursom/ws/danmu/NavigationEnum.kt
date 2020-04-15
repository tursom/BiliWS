package cn.tursom.ws.danmu

enum class NavigationEnum(val code: Int) {
  NONE(0),
  /**
   * 总督
   */
  GOVERNOR(1),
  /**
   * 提督
   */
  ADMIRAL(2),
  /**
   * 舰长
   */
  CAPTAIN(3);

  companion object {
    fun valueOf(code: Int): NavigationEnum = when (code) {
      0 -> NONE
      1 -> GOVERNOR
      2 -> ADMIRAL
      3 -> CAPTAIN
      else -> throw IndexOutOfBoundsException()
    }
  }
}