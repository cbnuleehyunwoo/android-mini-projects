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
    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination = _destination.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val session =
                runCatching {
                    authRepository.loadSessionFromStorage()
                        ?: authRepository.getCurrentSession()
                }.getOrNull()

            if (session == null) {
                _destination.update { SplashDestination.LOGIN }
                return@launch
            }

            val profile =
                runCatching {
                    profileRepository.getMyProfile()
                }.getOrNull()

            _destination.update {
                if (profile == null) {
                    SplashDestination.TERMS_AGREEMENT
                } else {
                    SplashDestination.HOME
                }
            }
        }
    }

    fun onDestinationHandled() {
        _destination.update { null }
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
