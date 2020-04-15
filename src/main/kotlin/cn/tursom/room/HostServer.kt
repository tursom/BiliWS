package cn.tursom.room

data class HostServer(
  val host: String,
  val port: Int,
  val ws_port: Int?,
  val wss_port: Int?
)