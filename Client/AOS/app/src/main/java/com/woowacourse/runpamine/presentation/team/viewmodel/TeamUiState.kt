package com.woowacourse.runpamine.presentation.team.viewmodel

import com.woowacourse.runpamine.presentation.team.model.TeamMember

data class TeamUiState(
    val hasTeam: Boolean = false,
    val teamName: String = "",
    val joinCode: String = "",
    val date: String = "",
    val totalDistance: String = "",
    val completedMemberCount: Int = 0,
    val totalMemberCount: Int = 0,
    val members: List<TeamMember> = emptyList(),
    val isLoading: Boolean = true,
    val isDateLoading: Boolean = false,
    val isLeavingTeam: Boolean = false,
    val isTeamLeft: Boolean = false,
    val canMoveToNextDate: Boolean = false,
    val errorMessage: String? = null,
    val memberErrorMessage: String? = null,
)
