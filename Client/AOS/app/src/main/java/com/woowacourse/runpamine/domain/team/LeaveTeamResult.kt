package com.woowacourse.runpamine.domain.team

data class LeaveTeamResult(
    val left: Boolean,
    val teamDeleted: Boolean,
    val newOwnerId: String?,
)
