package cn.tursom.storage.impl

import cn.tursom.storage.LiveTime

class MapLiveTime(
  private val map: MutableMap<Int, Long> = HashMap()
) : LiveTime {
  override fun getLiveTime(roomId: Int): Long {
    return map[roomId] ?: 0
  }

  override fun roomOnLive(roomId: Int) {
    map[roomId] = System.currentTimeMillis()
  }
}