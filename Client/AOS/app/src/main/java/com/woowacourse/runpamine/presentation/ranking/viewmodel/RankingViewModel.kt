package com.woowacourse.runpamine.presentation.ranking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.woowacourse.runpamine.domain.profile.ProfileRepository
import com.woowacourse.runpamine.domain.ranking.RankingMetric
import com.woowacourse.runpamine.domain.ranking.RankingRepository
import com.woowacourse.runpamine.presentation.ranking.model.RankingScope
import com.woowacourse.runpamine.presentation.ranking.model.RankingUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RankingViewModel(
    private val rankingRepository: RankingRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RankingUiState(isLoading = true))
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    init {
        loadRankings()
    }

    fun selectScope(scope: RankingScope) {
        if (_uiState.value.selectedScope == scope) return
        _uiState.update { it.copy(selectedScope = scope) }
        loadRankings()
    }

    fun selectMetric(metric: RankingMetric) {
        if (_uiState.value.selectedMetric == metric) return
        _uiState.update { it.copy(selectedMetric = metric) }
        loadRankings()
    }

    fun retry() {
        loadRankings()
    }

    private fun loadRankings() {
        val scope = _uiState.value.selectedScope
        val metric = _uiState.value.selectedMetric

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
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
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        homeState = result.homeState,
                        myRankingSummary = result.mySummary,
                        userRankings = result.userRankings ?: it.userRankings,
                        teamRankings = result.teamRankings ?: it.teamRankings,
                        isLoading = false,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "랭킹을 불러오지 못했어요.",
                    )
                }
            }
        }
    }

    class Factory(
        private val rankingRepository: RankingRepository,
        private val profileRepository: ProfileRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(RankingViewModel::class.java))
            return RankingViewModel(
                rankingRepository = rankingRepository,
                profileRepository = profileRepository,
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
