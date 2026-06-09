package com.woowacourse.runpamine.domain.team

data class TeamDailySummary(
    val team: Team,
    val date: String,
    val teamTotalDistanceMeters: Int,
    val completedMemberCount: Int,
    val totalMemberCount: Int,
    val members: List<TeamRunSummary>,
)

data class TeamRunSummary(
    val userId: String,
    val nickname: String,
    val distanceMeters: Int,
    val durationSeconds: Int,
    val averagePaceSecondsPerKm: Int,
    val calories: Int,
    val completed: Boolean,
)
