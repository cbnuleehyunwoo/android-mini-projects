package com.woowacourse.runpamine.presentation.ranking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.woowacourse.runpamine.domain.profile.ProfileRepository
import com.woowacourse.runpamine.domain.ranking.RankingMetric
import com.woowacourse.runpamine.domain.ranking.RankingRepository
import com.woowacourse.runpamine.presentation.cache.RankingCache
import com.woowacourse.runpamine.presentation.component.LoadingUiTiming
import com.woowacourse.runpamine.presentation.ranking.model.RankingScope
import com.woowacourse.runpamine.presentation.ranking.model.RankingUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.TimeSource

class RankingViewModel(
    private val rankingRepository: RankingRepository,
    private val profileRepository: ProfileRepository,
    private val cache: RankingCache,
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(
            RankingUiState(
                selectedScope = cache.selectedScope,
                selectedMetric = cache.selectedMetric,
                userRankings = cache.userRankingsByMetric[cache.selectedMetric].orEmpty(),
                teamRankings = cache.teamRankingsByMetric[cache.selectedMetric].orEmpty(),
                myRankingSummary = cache.mySummary,
                isLoading = true,
            ),
        )
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    init {
        loadRankings()
    }

    fun selectScope(scope: RankingScope) {
        if (_uiState.value.selectedScope == scope) return
        cache.selectedScope = scope
        _uiState.update { it.copy(selectedScope = scope) }
        loadRankings()
    }

    fun selectMetric(metric: RankingMetric) {
        if (_uiState.value.selectedMetric == metric) return
        cache.selectedMetric = metric
        _uiState.update {
            it.copy(
                selectedMetric = metric,
                userRankings = cache.userRankingsByMetric[metric].orEmpty(),
                teamRankings = cache.teamRankingsByMetric[metric].orEmpty(),
            )
        }
        loadRankings()
    }

    fun retry() {
        loadRankings()
    }

    private fun loadRankings() {
        val scope = _uiState.value.selectedScope
        val metric = _uiState.value.selectedMetric
        val requestId = ++cache.requestId

        viewModelScope.launch {
            val shouldGateSkeleton =
                when (scope) {
                    RankingScope.TEAM -> _uiState.value.teamRankings.isEmpty()
                    RankingScope.PERSONAL -> _uiState.value.userRankings.isEmpty()
                }
            val loadingStartedAt = TimeSource.Monotonic.markNow()
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isSkeletonVisible = false,
                    errorMessage = null,
                )
            }
            val skeletonRevealJob =
                launch {
                    delay(LoadingUiTiming.REVEAL_DELAY_MILLIS)
                    if (shouldGateSkeleton && isCurrentRequest(requestId, scope, metric)) {
                        _uiState.update { it.copy(isSkeletonVisible = true) }
                    }
                }

            try {
                val loadResult =
                    runCatching {
                        val homeState = _uiState.value.homeState ?: profileRepository.getHomeState()
                        val mySummary = _uiState.value.myRankingSummary ?: rankingRepository.getMyRankingSummary()
                        when (scope) {
                            RankingScope.TEAM ->
                                RankingLoadResult(
                                    homeState = homeState,
                                    mySummary = mySummary,
                                    teamRankings = rankingRepository.getTeamRankings(metric),
                                )

                            RankingScope.PERSONAL ->
                                RankingLoadResult(
                                    homeState = homeState,
                                    mySummary = mySummary,
                                    userRankings = rankingRepository.getUserRankings(metric),
                                )
                        }
                    }

                if (!isCurrentRequest(requestId, scope, metric)) return@launch
                if (shouldGateSkeleton && LoadingUiTiming.hasReachedRevealDelay(loadingStartedAt)) {
                    _uiState.update { it.copy(isSkeletonVisible = true) }
                    LoadingUiTiming.awaitContentReveal(loadingStartedAt)
                }
                if (!isCurrentRequest(requestId, scope, metric)) return@launch

                loadResult
                    .onSuccess { result ->
                        result.userRankings?.let { cache.userRankingsByMetric[metric] = it }
                        result.teamRankings?.let { cache.teamRankingsByMetric[metric] = it }
                        cache.mySummary = result.mySummary
                        _uiState.update {
                            it.copy(
                                homeState = result.homeState,
                                myRankingSummary = result.mySummary,
                                userRankings = result.userRankings ?: it.userRankings,
                                teamRankings = result.teamRankings ?: it.teamRankings,
                                isLoading = false,
                                isSkeletonVisible = false,
                            )
                        }
                    }.onFailure { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isSkeletonVisible = false,
                                errorMessage = throwable.message ?: "랭킹을 불러오지 못했어요.",
                            )
                        }
                    }
            } finally {
                skeletonRevealJob.cancel()
            }
        }
    }

    private fun isCurrentRequest(
        requestId: Long,
        scope: RankingScope,
        metric: RankingMetric,
    ): Boolean =
        cache.requestId == requestId &&
            _uiState.value.selectedScope == scope &&
            _uiState.value.selectedMetric == metric

    class Factory(
        private val rankingRepository: RankingRepository,
        private val profileRepository: ProfileRepository,
        private val cache: RankingCache,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(RankingViewModel::class.java))
            return RankingViewModel(
                rankingRepository = rankingRepository,
                profileRepository = profileRepository,
                cache = cache,
            ) as T
        }
    }
}

private data class RankingLoadResult(
    val homeState: com.woowacourse.runpamine.domain.profile.HomeState,
    val mySummary: com.woowacourse.runpamine.domain.ranking.MyRankingSummary,
    val userRankings: List<com.woowacourse.runpamine.domain.ranking.UserRanking>? = null,
    val teamRankings: List<com.woowacourse.runpamine.domain.ranking.TeamRanking>? = null,
)
