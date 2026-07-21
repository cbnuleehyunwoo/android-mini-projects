package com.woowacourse.runpamine.domain.ranking

data class UserRanking(
    val rank: Int,
    val userId: String,
    val nickname: String,
    val avatarKey: String?,
    val teamId: String?,
    val teamName: String?,
    val distanceMeters: Int,
    val durationSeconds: Long,
    val averagePaceSecondsPerKm: Int,
    val runCount: Int,
    val activeDays: Int,
    val elapsedDays: Int,
    val consistencyRate: Int,
)
