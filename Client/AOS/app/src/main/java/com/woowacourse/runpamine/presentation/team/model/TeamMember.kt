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
    val seasonDistance: String = "0.0",
    val seasonDuration: String = "0:00",
    val seasonRunCount: Int = 0,
    val seasonAveragePace: String = "-",
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
