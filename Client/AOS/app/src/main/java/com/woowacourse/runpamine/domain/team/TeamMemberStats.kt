package com.woowacourse.runpamine.domain.team

data class TeamMemberStats(
    val id: String,
    val nickname: String,
    val avatarKey: String?,
    val teamJoinedAt: String,
    val distanceMeters: Int,
    val durationSeconds: Int,
    val calories: Int,
    val runCount: Int,
    val activeDays: Int,
    val averagePaceSecondsPerKm: Int?,
    val recentRunDays: Int,
)
