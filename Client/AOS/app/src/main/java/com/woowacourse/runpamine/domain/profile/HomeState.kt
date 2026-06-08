package com.woowacourse.runpamine.domain.profile

data class HomeState(
    val profile: UserProfile?,
    val team: TeamSummary?,
)

data class TeamSummary(
    val id: String,
    val name: String,
    val joinCode: String?,
    val ownerId: String?,
    val memberCount: Int,
    val isOwner: Boolean,
)
