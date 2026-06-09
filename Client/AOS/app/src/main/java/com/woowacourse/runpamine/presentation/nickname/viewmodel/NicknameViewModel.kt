package com.woowacourse.runpamine.presentation.nickname.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.woowacourse.runpamine.domain.profile.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NicknameViewModel(
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NicknameUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadMyProfile()
    }

    private fun loadMyProfile() {
        viewModelScope.launch {
            runCatching {
                profileRepository.getMyProfile()
            }.onSuccess { profile ->
                if (profile != null) {
                    _uiState.update {
                        it.copy(
                            nickname = profile.nickname,
                            hasProfile = true,
                            errorMessage = null,
                        )
                    }
                }
            }
        }
    }

    fun updateNickname(input: String) {
        if (input.length <= NICKNAME_MAX_LENGTH) {
            _uiState.update {
                it.copy(
                    nickname = input,
                    errorMessage = null,
                )
            }
        }
    }

    fun submitProfile() {
        val currentState = _uiState.value
        if (!currentState.isValid || currentState.isLoading) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                )
            }

            runCatching {
                if (currentState.hasProfile) {
                    profileRepository.updateMyProfile(currentState.nickname)
                } else {
                    profileRepository.createProfile(currentState.nickname)
                }
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isCompleted = true,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "닉네임 설정에 실패했어요.",
                    )
                }
            }
        }
    }

    fun onCompletedHandled() {
        _uiState.update {
            it.copy(isCompleted = false)
        }
    }

    class Factory(
        private val profileRepository: ProfileRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(NicknameViewModel::class.java))
            return NicknameViewModel(profileRepository) as T
        }
    }
}
