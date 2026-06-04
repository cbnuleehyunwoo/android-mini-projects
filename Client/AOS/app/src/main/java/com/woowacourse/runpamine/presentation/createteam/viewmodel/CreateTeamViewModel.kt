package com.woowacourse.runpamine.presentation.createteam.viewmodel

import androidx.lifecycle.ViewModel
import com.woowacourse.runpamine.presentation.createteam.model.CreateTeamUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CreateTeamViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTeamUiState())
    val uiState: StateFlow<CreateTeamUiState> = _uiState.asStateFlow()

    fun updateTeamName(teamName: String) {
        _uiState.update {
            it.copy(
                teamName = teamName,
                isLengthValid = isLengthValid(teamName),
                hasAllowedCharacters = hasAllowedCharacters(teamName),
                hasNoSpecialCharacters = hasNoSpecialCharacters(teamName),
            )
        }
    }

    // 2~6자의 한글, 영문, 숫자만 허용하고 특수문자는 사용할 수 없다.
    fun isValidTeamName(teamName: String): Boolean =
        TEAM_NAME_REGEX.matches(teamName)

    // 2-6자 이내
    private fun isLengthValid(teamName: String): Boolean =
        teamName.length in MIN_LENGTH..MAX_LENGTH

    // 한글, 영문, 숫자만으로 이루어졌는지 (공백·특수문자 불가)
    private fun hasAllowedCharacters(teamName: String): Boolean =
        teamName.isNotEmpty() && ALLOWED_CHARACTERS_REGEX.matches(teamName)

    // 특수문자가 포함되지 않았는지
    private fun hasNoSpecialCharacters(teamName: String): Boolean =
        teamName.isNotEmpty() && !SPECIAL_CHARACTER_REGEX.containsMatchIn(teamName)

    companion object {
        private const val MIN_LENGTH = 2
        private const val MAX_LENGTH = 6
        private val TEAM_NAME_REGEX = Regex("^[가-힣a-zA-Z0-9]{2,6}$")
        private val ALLOWED_CHARACTERS_REGEX = Regex("^[가-힣a-zA-Z0-9]+$")
        private val SPECIAL_CHARACTER_REGEX = Regex("[^가-힣a-zA-Z0-9\\s]")
    }
}
