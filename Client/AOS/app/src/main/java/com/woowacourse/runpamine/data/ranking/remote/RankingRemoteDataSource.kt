package com.woowacourse.runpamine.data.ranking.remote

import com.woowacourse.runpamine.domain.ranking.MyRankingSummary
import com.woowacourse.runpamine.domain.ranking.RankingMetric
import com.woowacourse.runpamine.domain.ranking.TeamRanking
import com.woowacourse.runpamine.domain.ranking.UserRanking

interface RankingRemoteDataSource {
    suspend fun getTeamRankings(accessToken: String): List<TeamRanking>

    suspend fun getUserRankings(
        accessToken: String,
        metric: RankingMetric,
    ): List<UserRanking>

    suspend fun getMyRankingSummary(accessToken: String): MyRankingSummary
}
