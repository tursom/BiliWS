package cn.tursom.storage

interface LiveTime {
  fun getLiveTime(roomId: Int): Long
  fun roomOnLive(roomId: Int)
}