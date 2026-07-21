package com.woowacourse.runpamine.domain.team

data class TeamMemberSeasonStats(
    val id: String,
    val nickname: String,
    val avatarKey: String?,
    val teamJoinedAt: String,
    val seasonDistanceMeters: Int,
    val seasonDurationSeconds: Int,
    val seasonCalories: Int,
    val seasonRunCount: Int,
    val seasonActiveDays: Int,
    val averagePaceSecondsPerKm: Int?,
    val consecutiveRunDays: Int,
)
