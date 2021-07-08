package cn.tursom.ws.danmu

import cn.tursom.core.cast
import cn.tursom.core.toJson
import cn.tursom.danmu.Record

data class DanmuInfo(
  val metaData: DanmuMetaData,
  val danmu: String,
  val userInfo: DanmuUserInfo,
  val brandInfo: DanmuBrandInfo?,
  val userLevel: DanmuUserLevel,
  val userTitle: String,
  val navigation: NavigationEnum,
  val originData: List<Any>,
) {
  fun toProtobuf(): Record.DanmuInfo = Record.DanmuInfo.newBuilder()
    .setMetaData(metaData.toProtobuf())
    .setDanmu(danmu)
    .setUserInfo(userInfo.toProtobuf())
    .setBrandInfo(brandInfo?.toProtobuf() ?: Record.DanmuBrandInfo.getDefaultInstance())
    .setUserLevel(userLevel.toProtobuf())
    .setUserTitle(userTitle)
    .setNavigation(navigation.protobufValue)
    .setOriginData(originData.toJson())
    .build()

  companion object {
    fun parse(danmu: List<Any>): DanmuInfo {
      return DanmuInfo(
        metaData = DanmuMetaData.parse(danmu[0].cast()),
        danmu = danmu[1].cast(),
        userInfo = DanmuUserInfo.parse(danmu[2].cast()),
        brandInfo = DanmuBrandInfo.parse(danmu[3].cast()),
        userLevel = DanmuUserLevel.parse(danmu[4].cast()),
        userTitle = danmu[5].cast<Collection<String>>().first(),
        navigation = NavigationEnum.valueOf(danmu[7].cast<Number>().toInt()),
        originData = danmu
      )
    }
  }

  override fun toString(): String {
    return "DanmuInfo(metaData=$metaData, danmu='$danmu', userInfo=$userInfo, brandInfo=$brandInfo, userLevel=$userLevel, userTitle='$userTitle', navigation=$navigation)"
  }
}