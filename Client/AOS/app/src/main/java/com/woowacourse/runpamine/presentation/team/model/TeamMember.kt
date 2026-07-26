package com.woowacourse.runpamine.presentation.team.model

data class TeamMember(
    val id: String,
    val name: String,
    val distance: String,
    val time: String,
    val pace: String,
    val calories: String,
    val runningStatus: RunningStatus = RunningStatus.Resting,
    val teamJoinedAt: String = "",
    val totalDistance: String = "0.0",
    val totalDuration: String = "0:00",
    val totalRunCount: Int = 0,
    val averagePace: String = "-",
    val isMe: Boolean = false,
    val hasTodayRunRecord: Boolean = false,
)

enum class RunningStatus {
    LongResting,
    Resting,
    Running,
    ThreeDayRunning,
    FiveDayRunning,
}
