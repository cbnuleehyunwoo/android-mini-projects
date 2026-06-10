package com.woowacourse.runpamine.domain.team

data class TeamMemberSeasonStats(
    val id: String,
    val nickname: String,
    val avatarKey: String?,
    val consecutiveRunDays: Int,
)
