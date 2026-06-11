package com.woowacourse.runpamine.domain.ranking

interface RankingRepository {
    suspend fun getTeamRankings(metric: RankingMetric): List<TeamRanking>

    suspend fun getUserRankings(metric: RankingMetric): List<UserRanking>

    suspend fun getMyRankingSummary(): MyRankingSummary
}
