package com.woowacourse.runpamine.domain.ranking

data class MyRankingSummary(
    val season: RankingSeason,
    val eligible: Boolean,
    val requiredDistanceMeters: Int,
    val distanceMeters: Int,
    val remainingDistanceMeters: Int,
    val durationSeconds: Long,
    val averagePaceSecondsPerKm: Int,
    val runCount: Int,
    val activeDays: Int,
    val consistencyRate: Int,
    val distanceRank: Int?,
    val distanceTopPercent: Double?,
    val paceRank: Int?,
    val paceTopPercent: Double?,
    val consistencyRank: Int?,
    val consistencyTopPercent: Double?,
)
