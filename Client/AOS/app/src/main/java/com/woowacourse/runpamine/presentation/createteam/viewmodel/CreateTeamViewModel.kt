package com.woowacourse.runpamine.presentation.createteam.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.woowacourse.runpamine.domain.team.TeamRepository
import com.woowacourse.runpamine.presentation.createteam.model.CreateTeamUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateTeamViewModel(
    private val teamRepository: TeamRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateTeamUiState())
    val uiState: StateFlow<CreateTeamUiState> = _uiState.asStateFlow()

    fun updateTeamName(teamName: String) {
        _uiState.update {
            it.copy(
                teamName = teamName,
                isLengthValid = isLengthValid(teamName),
                hasAllowedCharacters = hasAllowedCharacters(teamName),
                hasNoSpecialCharacters = hasNoSpecialCharacters(teamName),
                errorMessage = null,
                createdJoinCode = null,
            )
        }
    }

    fun createTeam() {
        val teamName = uiState.value.teamName.trim()
        if (!isValidTeamName(teamName) || uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    createdJoinCode = null,
                )
            }
            runCatching {
                teamRepository.createTeam(teamName)
            }.onSuccess { team ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        createdJoinCode = team.joinCode,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "팀 생성에 실패했어요.",
                    )
                }
            }
        }
    }

    // 2~10자의 한글, 영문, 숫자만 허용하고 특수문자는 사용할 수 없다.
    fun isValidTeamName(teamName: String): Boolean = TEAM_NAME_REGEX.matches(teamName)

    // 2-10자 이내
    private fun isLengthValid(teamName: String): Boolean = teamName.length in MIN_LENGTH..MAX_LENGTH

    // 한글, 영문, 숫자만으로 이루어졌는지 (공백·특수문자 불가)
    private fun hasAllowedCharacters(teamName: String): Boolean = teamName.isNotEmpty() && ALLOWED_CHARACTERS_REGEX.matches(teamName)

    // 특수문자가 포함되지 않았는지
    private fun hasNoSpecialCharacters(teamName: String): Boolean =
        teamName.isNotEmpty() && !SPECIAL_CHARACTER_REGEX.containsMatchIn(teamName)

    companion object {
        private const val MIN_LENGTH = 2
        private const val MAX_LENGTH = 10
        private val TEAM_NAME_REGEX = Regex("^[가-힣a-zA-Z0-9]{2,10}$")
        private val ALLOWED_CHARACTERS_REGEX = Regex("^[가-힣a-zA-Z0-9]+$")
        private val SPECIAL_CHARACTER_REGEX = Regex("[^가-힣a-zA-Z0-9\\s]")
    }

    class Factory(
        private val teamRepository: TeamRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(CreateTeamViewModel::class.java))
            return CreateTeamViewModel(teamRepository) as T
        }
    }
}
