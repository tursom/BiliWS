package cn.tursom.ws.danmu

import cn.tursom.core.util.UncheckedCast
import cn.tursom.core.util.cast
import cn.tursom.core.util.toJson
import cn.tursom.danmu.Danmu

@OptIn(UncheckedCast::class)
data class DanmuInfo(
  val metadata: DanmuMetadata,
  val danmu: String,
  val userInfo: DanmuUserInfo,
  val brandInfo: DanmuBrandInfo?,
  val userLevel: DanmuUserLevel,
  val userTitle: String,
  val navigation: NavigationEnum,
  val originData: List<Any>,
) {
  fun toProtobuf(originData: Boolean = false): Danmu.DanmuInfo {
    val builder = Danmu.DanmuInfo.newBuilder()
      .setMetadata(metadata.toProtobuf())
      .setDanmu(danmu)
      .setUserInfo(userInfo.toProtobuf())
      .setBrandInfo(brandInfo?.toProtobuf() ?: Danmu.DanmuBrandInfo.getDefaultInstance())
      .setUserLevel(userLevel.toProtobuf())
      .setUserTitle(userTitle)
      .setNavigation(navigation.protobufValue)
    if (originData) {
      builder.originData = this.originData.toJson()
    }
    return builder.build()
  }

  companion object {
    fun parse(danmu: List<Any>): DanmuInfo {
      return DanmuInfo(
        metadata = DanmuMetadata.parse(danmu[0].cast()),
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
    return "DanmuInfo(metaData=$metadata, danmu='$danmu', userInfo=$userInfo, brandInfo=$brandInfo, userLevel=$userLevel, userTitle='$userTitle', navigation=$navigation)"
  }
}