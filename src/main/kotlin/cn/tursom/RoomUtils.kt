package cn.tursom

import cn.tursom.core.HttpRequest
import cn.tursom.core.println
import cn.tursom.room.*
import cn.tursom.utils.fromJson

@Suppress("MemberVisibilityCanBePrivate")
object RoomUtils {
  const val roomInfoUrl = "https://api.live.bilibili.com/room/v1/Room/get_info"
  fun getRoomInfo(roomId: Int): RoomInfoData {
    //val url = "https://api.live.bilibili.com/room/v1/Room/get_info?id=$roomId&from=room".println()
    val room = HttpRequest.doGet(
      roomInfoUrl,
      param = mapOf(
        "id" to roomId.toString(),
        "from" to "room"
      ),
      headers = getBiliLiveJsonAPIHeaders(roomId)
    ).fromJson<RoomInfo>()
    return room.data
  }

  const val liveUserInfoUrl = "https://api.live.bilibili.com/live_user/v1/UserInfo/get_anchor_in_room"
  fun getLiveUserInfo(roomId: Int): LiveUserData {
    //val url = "https://api.live.bilibili.com/live_user/v1/UserInfo/get_anchor_in_room?roomid=$roomId"
    //val liveUser = sendGet(url, getBiliLiveJsonAPIHeaders(roomId)).println().fromJson<LiveUser>()
    val liveUser = HttpRequest.doGet(
      liveUserInfoUrl,
      param = mapOf(
        "roomid" to roomId.toString()
      ),
      headers = getBiliLiveJsonAPIHeaders(roomId)
    ).fromJson<LiveUser>()
    return liveUser.data
  }

  const val liveInfoUrl = "https://api.live.bilibili.com/room/v1/Room/playUrl"
  fun getLiveInfo(roomId: Int, qn: Int): PlayUrlData {
    //val url = "https://api.live.bilibili.com/room/v1/Room/playUrl?cid=$roomId&quality=$qn&platform=web"
    val liveUser = HttpRequest.doGet(
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
  fun getLiveServerConf(): WsServerConf {
    return HttpRequest.doGet(liveServer).fromJson()
  }

  /**
   * 该Header配置用于直播 api 信息查询
   */
  fun getBiliLiveJsonAPIHeaders(shortId: Int): HashMap<String, String> {
    val headerMap = HashMap<String, String>()
    headerMap["Accept"] = "application/json, text/javascript, */*; q=0.01"
    headerMap["Accept-Encoding"] = "gzip, deflate, sdch, br"
    headerMap["Accept-Language"] = "zh-CN,zh;q=0.8"
    headerMap["Connection"] = "keep-alive"
    headerMap["Host"] = "api.bilibili.com"
    headerMap["Origin"] = "https://live.bilibili.com"
    headerMap["Referer"] = "https://live.bilibili.com/blanc/$shortId" // need addavId
    headerMap["User-Agent"] =
      "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36 SE 2.X MetaSr 1.0"
    headerMap["X-Requested-With"] = "ShockwaveFlash/28.0.0.137"
    return headerMap
  }
}

fun main() {
  val template =
    "z2cl_6b5PhB-wykK792f17CIcFhhYEuWKxOyO97svGONiq6TEUL2BDg3IsGkKVbXlle4ET1E_eOY6v66_eaiW4VblUJmtz_EB5mnulrhtgRA"
  HttpRequest.doGet(
    "https://api.live.bilibili.com/room/v1/Danmu/getConf", mapOf(
      "id" to "4767523"
    )
  ).println()
}