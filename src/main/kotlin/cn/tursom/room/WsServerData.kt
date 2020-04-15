package cn.tursom.room

data class WsServerData(
  val host: String,
  val host_server_list: List<HostServer>,
  val max_delay: Int,
  val port: Int,
  val refresh_rate: Int,
  val refresh_row_factor: Double,
  val server_list: List<Server>,
  val token: String
)