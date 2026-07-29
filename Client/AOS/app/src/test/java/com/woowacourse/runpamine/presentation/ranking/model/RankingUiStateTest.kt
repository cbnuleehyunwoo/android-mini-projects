package com.woowacourse.runpamine.presentation.ranking.model

import com.woowacourse.runpamine.domain.ranking.TeamRanking
import com.woowacourse.runpamine.domain.ranking.UserRanking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RankingUiStateTest {
    @Test
    fun `팀 랭킹 캐시가 있으면 갱신 중에도 스켈레톤을 표시하지 않는다`() {
        val state =
            RankingUiState(
                selectedScope = RankingScope.TEAM,
                teamRankings = listOf(teamRanking),
                isLoading = true,
            )

        assertFalse(state.shouldShowLoadingSkeleton)
    }

    @Test
    fun `개인 랭킹 캐시가 있으면 갱신 중에도 스켈레톤을 표시하지 않는다`() {
        val state =
            RankingUiState(
                selectedScope = RankingScope.PERSONAL,
                userRankings = listOf(userRanking),
                isLoading = true,
            )

        assertFalse(state.shouldShowLoadingSkeleton)
    }

    @Test
    fun `선택한 랭킹에 캐시가 없으면 갱신 중 스켈레톤을 표시한다`() {
        val state =
            RankingUiState(
                selectedScope = RankingScope.TEAM,
                userRankings = listOf(userRanking),
                isLoading = true,
            )

        assertTrue(state.shouldShowLoadingSkeleton)
    }

    @Test
    fun `로딩 중이 아니면 캐시가 없어도 스켈레톤을 표시하지 않는다`() {
        val state =
            RankingUiState(
                selectedScope = RankingScope.TEAM,
                isLoading = false,
            )

        assertFalse(state.shouldShowLoadingSkeleton)
    }

    private companion object {
        val teamRanking =
            TeamRanking(
                rank = 1,
                teamId = "team-id",
                teamName = "런파민",
                distanceMeters = 10_000,
                durationSeconds = 3_600,
                averagePaceSecondsPerKm = 360,
                runCount = 1,
                totalActiveDays = 1,
                averageActiveDays = 1.0,
            )

        val userRanking =
            UserRanking(
                rank = 1,
                userId = "user-id",
                nickname = "러너",
                avatarKey = null,
                teamId = "team-id",
                teamName = "런파민",
                distanceMeters = 10_000,
                durationSeconds = 3_600,
                averagePaceSecondsPerKm = 360,
                runCount = 1,
                activeDays = 1,
                elapsedDays = 1,
                consistencyRate = 100,
            )
    }
}
