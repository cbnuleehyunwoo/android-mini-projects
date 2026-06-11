package com.woowacourse.runpamine.presentation.mypage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.woowacourse.runpamine.domain.auth.AuthRepository
import com.woowacourse.runpamine.domain.profile.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MyPageViewModel(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadMyProfile()
    }

    fun loadMyProfile() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                )
            }
            runCatching {
                profileRepository.getMyProfile()
            }.onSuccess { profile ->
                _uiState.update {
                    it.copy(
                        nickname = profile?.nickname.orEmpty(),
                        isLoading = false,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "프로필 정보를 불러오지 못했어요.",
                    )
                }
            }
        }
    }

    fun logout() {
        if (_uiState.value.isLoggingOut) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoggingOut = true,
                    errorMessage = null,
                )
            }

            runCatching {
                authRepository.signOut()
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoggingOut = false,
                        isLoggedOut = true,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoggingOut = false,
                        errorMessage = throwable.message ?: "로그아웃에 실패했어요.",
                    )
                }
            }
        }
    }

    class Factory(
        private val profileRepository: ProfileRepository,
        private val authRepository: AuthRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(MyPageViewModel::class.java))
            return MyPageViewModel(
                profileRepository = profileRepository,
                authRepository = authRepository,
            ) as T
        }
    }
}
