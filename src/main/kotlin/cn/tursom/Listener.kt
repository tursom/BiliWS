package cn.tursom

interface Listener {
  val cancelled: Boolean
  fun cancel()
}