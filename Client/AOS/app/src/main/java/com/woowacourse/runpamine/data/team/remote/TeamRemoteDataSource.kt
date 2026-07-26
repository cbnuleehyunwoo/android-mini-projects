package com.woowacourse.runpamine.data.team.remote

import com.woowacourse.runpamine.domain.team.LeaveTeamResult
import com.woowacourse.runpamine.domain.team.Team
import com.woowacourse.runpamine.domain.team.TeamDailySummary
import com.woowacourse.runpamine.domain.team.TeamMemberStats
import com.woowacourse.runpamine.domain.team.TeamMemberSummary
import java.time.LocalDate

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

    suspend fun getMyTeamStats(accessToken: String): List<TeamMemberStats>

    suspend fun getMyTeamDailySummary(
        accessToken: String,
        date: LocalDate,
    ): TeamDailySummary

    suspend fun leaveTeam(accessToken: String): LeaveTeamResult
}
