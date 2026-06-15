package com.woowacourse.runpamine.domain.team

import java.time.LocalDate

interface TeamRepository {
    suspend fun createTeam(name: String): Team

    suspend fun joinTeam(joinCode: String): Team

    suspend fun getMyTeam(): Team

    suspend fun getMyTeamMembers(): List<TeamMemberSummary>

    suspend fun getMyTeamSeasonStats(): List<TeamMemberSeasonStats>

    suspend fun getMyTeamDailySummary(date: LocalDate): TeamDailySummary
}
