package com.woowacourse.runpamine.domain.ranking

data class TeamRanking(
    val rank: Int,
    val teamId: String,
    val teamName: String,
    val distanceMeters: Int,
    val durationSeconds: Long,
    val averagePaceSecondsPerKm: Int,
    val runCount: Int,
    val totalActiveDays: Int,
    val averageActiveDays: Double,
)
