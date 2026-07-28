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
    private val clearLocalUserData: suspend () -> Unit,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState = _uiState.asStateFlow()

    fun loadMyProfile() {
        profileRepository.getCachedProfile()?.let { profile ->
            _uiState.update {
                it.copy(
                    nickname = profile.nickname,
                    errorMessage = null,
                )
            }
            return
        }

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
        if (_uiState.value.isBusy) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoggingOut = true,
                    errorMessage = null,
                )
            }

            val signOutError =
                runCatching {
                    authRepository.signOut()
                }.exceptionOrNull()
            runCatching {
                clearLocalUserData()
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoggingOut = false,
                        isLoggedOut = true,
                        errorMessage = null,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoggingOut = false,
                        errorMessage = throwable.message ?: signOutError?.message ?: "로그아웃에 실패했어요.",
                    )
                }
            }
        }
    }

    fun deleteAccount() {
        if (_uiState.value.isBusy) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isDeletingAccount = true,
                    errorMessage = null,
                )
            }

            val deleteAccountError =
                runCatching {
                    authRepository.deleteAccount()
                }.exceptionOrNull()
            runCatching {
                clearLocalUserData()
            }.onSuccess {
                if (deleteAccountError != null) {
                    _uiState.update {
                        it.copy(
                            isDeletingAccount = false,
                            errorMessage = deleteAccountError.message ?: "회원 탈퇴에 실패했어요.",
                        )
                    }
                    return@onSuccess
                }
                _uiState.update {
                    it.copy(
                        isDeletingAccount = false,
                        isLoggedOut = true,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isDeletingAccount = false,
                        errorMessage = throwable.message ?: "회원 탈퇴에 실패했어요.",
                    )
                }
            }
        }
    }

    fun onLoggedOutHandled() {
        _uiState.update {
            it.copy(isLoggedOut = false)
        }
    }

    class Factory(
        private val profileRepository: ProfileRepository,
        private val authRepository: AuthRepository,
        private val clearLocalUserData: suspend () -> Unit,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(MyPageViewModel::class.java))
            return MyPageViewModel(
                profileRepository = profileRepository,
                authRepository = authRepository,
                clearLocalUserData = clearLocalUserData,
            ) as T
        }
    }
}

private val MyPageUiState.isBusy: Boolean
    get() = isLoggingOut || isDeletingAccount
