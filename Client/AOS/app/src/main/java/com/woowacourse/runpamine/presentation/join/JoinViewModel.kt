package com.woowacourse.runpamine.presentation.join

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.woowacourse.runpamine.domain.team.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JoinViewModel(
    private val teamRepository: TeamRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(JoinUiState())
    val uiState = _uiState.asStateFlow()

    fun updateCode(code: String) {
        if (code.length <= JOIN_CODE_MAX_LENGTH && JOIN_CODE_REGEX.matches(code)) {
            _uiState.update {
                it.copy(
                    code = code.uppercase(),
                    errorMessage = null,
                    isJoined = false,
                )
            }
        }
    }

    fun joinTeam() {
        val joinCode = uiState.value.code.trim()
        if (joinCode.length != JOIN_CODE_MAX_LENGTH || uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    isJoined = false,
                )
            }
            runCatching {
                teamRepository.joinTeam(joinCode)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isJoined = true,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "팀 참가에 실패했어요.",
                    )
                }
            }
        }
    }

    class Factory(
        private val teamRepository: TeamRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(JoinViewModel::class.java))
            return JoinViewModel(teamRepository) as T
        }
    }
}

const val JOIN_CODE_MAX_LENGTH = 6
val JOIN_CODE_REGEX = Regex("^[a-zA-Z0-9]*$")
