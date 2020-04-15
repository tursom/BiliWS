package cn.tursom.room

data class PlayUrlData(
  val accept_quality: List<String>,
  val current_qn: Int,
  val current_quality: Int,
  val durl: List<Durl>,
  val quality_description: List<QualityDescription>
)