package cn.tursom.ws

data class SendMsgReply(
  val cmd: String,
  val info: List<Any>,
  val data: Map<String, Any>
)