package com.woowacourse.runpamine.data.team.remote

import com.woowacourse.runpamine.domain.team.Team
import com.woowacourse.runpamine.domain.team.TeamDailySummary
import com.woowacourse.runpamine.domain.team.TeamMemberSummary

interface TeamRemoteDataSource {
    suspend fun createTeam(
        accessToken: String,
        name: String,
    ): Team

    suspend fun joinTeam(
        accessToken: String,
        joinCode: String,
    ): Team

    suspend fun getMyTeam(accessToken: String): Team

    suspend fun getMyTeamMembers(accessToken: String): List<TeamMemberSummary>

    suspend fun getMyTeamDailySummary(accessToken: String): TeamDailySummary
}
