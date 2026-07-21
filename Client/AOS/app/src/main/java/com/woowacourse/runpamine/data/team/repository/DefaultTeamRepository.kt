package com.woowacourse.runpamine.data.team.repository

import com.woowacourse.runpamine.data.team.remote.TeamRemoteDataSource
import com.woowacourse.runpamine.domain.auth.AuthRepository
import com.woowacourse.runpamine.domain.team.Team
import com.woowacourse.runpamine.domain.team.TeamDailySummary
import com.woowacourse.runpamine.domain.team.TeamMemberSeasonStats
import com.woowacourse.runpamine.domain.team.TeamMemberSummary
import com.woowacourse.runpamine.domain.team.TeamRepository
import java.time.LocalDate

class DefaultTeamRepository(
    private val authRepository: AuthRepository,
    private val remoteDataSource: TeamRemoteDataSource,
) : TeamRepository {
    override suspend fun createTeam(name: String): Team =
        remoteDataSource.createTeam(
            accessToken = requireAccessToken(),
            name = name,
        )

    override suspend fun joinTeam(joinCode: String): Team =
        remoteDataSource.joinTeam(
            accessToken = requireAccessToken(),
            joinCode = joinCode,
        )

    override suspend fun getMyTeam(): Team =
        remoteDataSource.getMyTeam(
            accessToken = requireAccessToken(),
        )

    override suspend fun getMyTeamMembers(): List<TeamMemberSummary> =
        remoteDataSource.getMyTeamMembers(
            accessToken = requireAccessToken(),
        )

    override suspend fun getMyTeamSeasonStats(): List<TeamMemberSeasonStats> =
        remoteDataSource.getMyTeamSeasonStats(
            accessToken = requireAccessToken(),
        )

    override suspend fun getMyTeamDailySummary(date: LocalDate): TeamDailySummary =
        remoteDataSource.getMyTeamDailySummary(
            accessToken = requireAccessToken(),
            date = date,
        )

    override suspend fun leaveTeam() =
        remoteDataSource.leaveTeam(
            accessToken = requireAccessToken(),
        )

    private suspend fun requireAccessToken(): String =
        requireNotNull(authRepository.getCurrentSession()?.accessToken) {
            "로그인이 필요해요."
        }
}
