package cn.tursom.ws

import cn.tursom.exception.UnsupportedCodeException

enum class BiliLiveWSCode(val code: Int) {
  HANDSHAKE(0),
  HANDSHAKE_REPLY(1),
  HEARTBEAT(2),
  HEARTBEAT_REPLY(3),
  SEND_MSG(4),
  SEND_MSG_REPLY(5),
  DISCONNECT_REPLY(6),
  AUTH(7),
  AUTH_REPLY(8),
  RAW(9),
  PROTO_READY(10),
  PROTO_FINISH(11),
  CHANGE_ROOM(12),
  CHANGE_ROOM_REPLY(13),
  REGISTER(14),
  REGISTER_REPLY(15),
  UNREGISTER(16),
  UNREGISTER_REPLY(17);

  companion object {
    fun valueOf(code: Int): BiliLiveWSCode = when (code) {
      0 -> HANDSHAKE
      1 -> HANDSHAKE_REPLY
      2 -> HEARTBEAT
      3 -> HEARTBEAT_REPLY
      4 -> SEND_MSG
      5 -> SEND_MSG_REPLY
      6 -> DISCONNECT_REPLY
      7 -> AUTH
      8 -> AUTH_REPLY
      9 -> RAW
      10 -> PROTO_READY
      11 -> PROTO_FINISH
      12 -> CHANGE_ROOM
      13 -> CHANGE_ROOM_REPLY
      14 -> REGISTER
      15 -> REGISTER_REPLY
      16 -> UNREGISTER
      17 -> UNREGISTER_REPLY
      else -> throw UnsupportedCodeException()
    }
  }
}