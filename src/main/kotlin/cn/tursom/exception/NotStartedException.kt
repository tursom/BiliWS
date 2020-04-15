package cn.tursom.exception

class NotStartedException(roomId: Int?) : LiveException("房间 $roomId 未开播")