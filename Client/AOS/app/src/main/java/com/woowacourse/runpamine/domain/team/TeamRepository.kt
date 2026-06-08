package com.woowacourse.runpamine.domain.team

interface TeamRepository {
    suspend fun createTeam(name: String): Team

    suspend fun joinTeam(joinCode: String): Team

    suspend fun getMyTeam(): Team

    suspend fun getMyTeamMembers(): List<TeamMemberSummary>

    suspend fun getMyTeamDailySummary(): TeamDailySummary
}
