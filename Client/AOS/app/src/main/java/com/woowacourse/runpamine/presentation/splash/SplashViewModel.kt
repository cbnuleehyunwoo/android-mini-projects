package com.woowacourse.runpamine.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.woowacourse.runpamine.domain.auth.AuthRepository
import com.woowacourse.runpamine.domain.profile.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SplashViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            _uiState.update { SplashUiState.Loading }

            val session =
                runCatching {
                    authRepository.loadSessionFromStorage()
                        ?: authRepository.getCurrentSession()
                }.getOrNull()

            if (session == null) {
                _uiState.update {
                    SplashUiState.Completed(SplashDestination.LOGIN)
                }
                return@launch
            }

            val profileResult =
                runCatching {
                    profileRepository.getMyProfile()
                }
            _uiState.update { profileResult.toSplashUiState() }
        }
    }

    fun retry() {
        checkSession()
    }

    fun onDestinationHandled() {
        _uiState.update { SplashUiState.Loading }
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val profileRepository: ProfileRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(SplashViewModel::class.java))
            return SplashViewModel(
                authRepository = authRepository,
                profileRepository = profileRepository,
            ) as T
        }
    }
}
