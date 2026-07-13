package com.woowacourse.runpamine.presentation.cache

import com.woowacourse.runpamine.domain.ranking.MyRankingSummary
import com.woowacourse.runpamine.domain.ranking.RankingMetric
import com.woowacourse.runpamine.domain.ranking.TeamRanking
import com.woowacourse.runpamine.domain.ranking.UserRanking
import com.woowacourse.runpamine.domain.run.RunPeriodSummary
import com.woowacourse.runpamine.domain.run.RunSession
import com.woowacourse.runpamine.presentation.ranking.model.RankingScope
import com.woowacourse.runpamine.presentation.record.model.RecordPeriod
import com.woowacourse.runpamine.presentation.team.viewmodel.TeamUiState
import java.time.LocalDate
import java.time.YearMonth

class TeamDashboardCache {
    var state: TeamUiState? = null
    var selectedDate: LocalDate = LocalDate.now()

    fun clear() {
        state = null
        selectedDate = LocalDate.now()
    }
}

class RankingCache {
    var selectedScope: RankingScope = RankingScope.TEAM
    var selectedMetric: RankingMetric = RankingMetric.DISTANCE
    val teamRankingsByMetric = mutableMapOf<RankingMetric, List<TeamRanking>>()
    val userRankingsByMetric = mutableMapOf<RankingMetric, List<UserRanking>>()
    var mySummary: MyRankingSummary? = null
    var requestId: Long = 0L

    fun clear() {
        selectedScope = RankingScope.TEAM
        selectedMetric = RankingMetric.DISTANCE
        teamRankingsByMetric.clear()
        userRankingsByMetric.clear()
        mySummary = null
        requestId++
    }
}

class RecordCache {
    var selectedPeriod: RecordPeriod = RecordPeriod.WEEK
    var anchorDate: LocalDate = LocalDate.now()
    val summaries = mutableMapOf<RecordCacheKey, RunPeriodSummary>()
    val detailsByRunId = mutableMapOf<String, RunSession>()

    fun clear() {
        selectedPeriod = RecordPeriod.WEEK
        anchorDate = LocalDate.now()
        summaries.clear()
        detailsByRunId.clear()
    }
}

sealed interface RecordCacheKey {
    data class Week(
        val startDate: LocalDate,
    ) : RecordCacheKey

    data class Month(
        val yearMonth: YearMonth,
    ) : RecordCacheKey
}
