package com.woowacourse.runpamine.data.ranking.repository

import com.woowacourse.runpamine.data.ranking.remote.RankingRemoteDataSource
import com.woowacourse.runpamine.domain.auth.AuthRepository
import com.woowacourse.runpamine.domain.ranking.MyRankingSummary
import com.woowacourse.runpamine.domain.ranking.RankingMetric
import com.woowacourse.runpamine.domain.ranking.RankingRepository
import com.woowacourse.runpamine.domain.ranking.TeamRanking
import com.woowacourse.runpamine.domain.ranking.UserRanking

class DefaultRankingRepository(
    private val authRepository: AuthRepository,
    private val remoteDataSource: RankingRemoteDataSource,
) : RankingRepository {
    override suspend fun getTeamRankings(): List<TeamRanking> = remoteDataSource.getTeamRankings(requireAccessToken())

    override suspend fun getUserRankings(metric: RankingMetric): List<UserRanking> =
        remoteDataSource.getUserRankings(
            accessToken = requireAccessToken(),
            metric = metric,
        )

    override suspend fun getMyRankingSummary(): MyRankingSummary = remoteDataSource.getMyRankingSummary(requireAccessToken())

    private suspend fun requireAccessToken(): String =
        requireNotNull(authRepository.getCurrentSession()?.accessToken) {
            "로그인이 필요해요."
        }
}
