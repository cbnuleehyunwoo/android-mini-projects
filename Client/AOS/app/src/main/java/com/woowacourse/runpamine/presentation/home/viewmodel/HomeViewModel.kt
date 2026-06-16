package com.woowacourse.runpamine.presentation.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.woowacourse.runpamine.domain.profile.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadHomeState()
    }

    fun loadHomeState() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                )
            }

            runCatching {
                profileRepository.getHomeState()
            }.onSuccess { homeState ->
                _uiState.update {
                    it.copy(
                        nickname = homeState.profile?.nickname.orEmpty(),
                        teamName = homeState.team?.name,
                        teamMemberCount = homeState.team?.memberCount ?: 0,
                        todayRunMemberCount = homeState.team?.todayRunMemberCount ?: 0,
                        isLoading = false,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "홈 정보를 불러오지 못했어요.",
                    )
                }
            }
        }
    }

    class Factory(
        private val profileRepository: ProfileRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(HomeViewModel::class.java))
            return HomeViewModel(profileRepository) as T
        }
    }
}
