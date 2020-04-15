package cn.tursom.room

data class MasterLevel(
    val anchor_score: Int,
    val color: Int,
    val current: List<Int>,
    val level: Int,
    val master_level_color: Int,
    val next: List<Int>,
    val sort: String,
    val upgrade_score: Int
)