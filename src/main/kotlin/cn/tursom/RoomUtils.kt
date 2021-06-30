package cn.tursom

import cn.tursom.core.HttpRequest
import cn.tursom.core.fromJson
import cn.tursom.core.toUTF8String
import cn.tursom.room.*
import cn.tursom.utils.AsyncHttpRequest

@Suppress("MemberVisibilityCanBePrivate")
object RoomUtils {
  //const val roomInfoUrl = "https://api.live.bilibili.com/room/v1/Room/get_info"
  const val roomInfoUrl = "http://api.live.bilibili.com/room/v1/Room/room_init"
  suspend fun getRoomInfo(roomId: Int): RoomInfoData {
    //val url = "https://api.live.bilibili.com/room/v1/Room/get_info?id=$roomId&from=room".println()
    val response = AsyncHttpRequest.get(
      roomInfoUrl,
      param = mapOf(
        "id" to roomId.toString(),
        "from" to "room"
      ),
      headers = getBiliLiveJsonAPIHeaders(roomId)
    )
    val roomInfoStr = response.body()!!.string()
    val room = roomInfoStr.fromJson<RoomInfo>()
    return room.data
  }

  const val liveUserInfoUrl = "https://api.live.bilibili.com/live_user/v1/UserInfo/get_anchor_in_room"
  suspend fun getLiveUserInfo(roomId: Int): LiveUserData {
    //val url = "https://api.live.bilibili.com/live_user/v1/UserInfo/get_anchor_in_room?roomid=$roomId"
    //val liveUser = sendGet(url, getBiliLiveJsonAPIHeaders(roomId)).println().fromJson<LiveUser>()
    val liveUser = AsyncHttpRequest.getStr(
      liveUserInfoUrl,
      param = mapOf(
        "roomid" to roomId.toString()
      ),
      headers = getBiliLiveJsonAPIHeaders(roomId)
    ).fromJson<LiveUser>()
    return liveUser.data
  }

  const val liveInfoUrl = "https://api.live.bilibili.com/room/v1/Room/playUrl"
  suspend fun getLiveInfo(roomId: Int, qn: Int): PlayUrlData {
    //val url = "https://api.live.bilibili.com/room/v1/Room/playUrl?cid=$roomId&quality=$qn&platform=web"
    val liveUser = AsyncHttpRequest.getStr(
      liveInfoUrl,
      param = mapOf(
        "cid" to roomId.toString(),
        "quality" to qn.toString(),
        "platform" to "web"
      ),
      headers = getBiliLiveJsonAPIHeaders(roomId)
    ).fromJson<PlayUrl>()
    return liveUser.data
  }

  const val liveServer = "https://api.live.bilibili.com/room/v1/Danmu/getConf"
  suspend fun getLiveServerConf(): WsServerConf {
    return AsyncHttpRequest.getStr(liveServer).fromJson()
  }

  suspend fun getLiveServerConf(roomId: Int): WsServerConf {
    return AsyncHttpRequest.getStr(
      liveServer, mapOf(
        "room_id" to roomId.toString(), "platform" to "pc", "player" to "web"
      )
    ).fromJson()
  }

  /**
   * 该Header配置用于直播 api 信息查询
   */
  fun getBiliLiveJsonAPIHeaders(shortId: Int): HashMap<String, String> {
    val headerMap = HashMap<String, String>()
    headerMap["Origin"] = "https://live.bilibili.com"
    headerMap["Referer"] = "https://live.bilibili.com/blanc/$shortId" // need addavId
    headerMap["User-Agent"] =
      "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36 SE 2.X MetaSr 1.0"
    return headerMap
  }
}
