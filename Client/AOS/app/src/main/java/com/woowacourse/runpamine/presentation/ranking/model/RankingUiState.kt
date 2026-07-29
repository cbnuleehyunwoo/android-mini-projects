package com.woowacourse.runpamine.presentation.ranking.model

import com.woowacourse.runpamine.domain.profile.HomeState
import com.woowacourse.runpamine.domain.ranking.MyRankingSummary
import com.woowacourse.runpamine.domain.ranking.RankingMetric
import com.woowacourse.runpamine.domain.ranking.TeamRanking
import com.woowacourse.runpamine.domain.ranking.UserRanking

data class RankingUiState(
    val selectedScope: RankingScope = RankingScope.PERSONAL,
    val selectedMetric: RankingMetric = RankingMetric.DISTANCE,
    val userRankings: List<UserRanking> = emptyList(),
    val teamRankings: List<TeamRanking> = emptyList(),
    val myRankingSummary: MyRankingSummary? = null,
    val homeState: HomeState? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

val RankingUiState.shouldShowLoadingSkeleton: Boolean
    get() =
        isLoading &&
            when (selectedScope) {
                RankingScope.TEAM -> teamRankings.isEmpty()
                RankingScope.PERSONAL -> userRankings.isEmpty()
            }

enum class RankingScope(
    val label: String,
) {
    TEAM("팀 랭킹"),
    PERSONAL("개인 랭킹"),
}
