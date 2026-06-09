package com.woowacourse.runpamine.domain.team

data class Team(
    val id: String,
    val name: String,
    val joinCode: String,
    val ownerId: String,
    val memberCount: Int,
    val isOwner: Boolean,
)
