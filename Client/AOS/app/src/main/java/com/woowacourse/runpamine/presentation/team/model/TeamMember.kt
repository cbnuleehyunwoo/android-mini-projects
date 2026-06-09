package com.woowacourse.runpamine.presentation.team.model

data class TeamMember(
    val id: String,
    val name: String,
    val distance: String,
    val time: String,
    val pace: String,
    val calories: String,
    val hasRunRecord: Boolean = false,
)
