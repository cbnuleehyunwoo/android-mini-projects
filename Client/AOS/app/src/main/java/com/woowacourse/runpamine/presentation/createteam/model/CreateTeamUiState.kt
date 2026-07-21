package com.woowacourse.runpamine.presentation.createteam.model

data class CreateTeamUiState(
    val teamName: String = "",
    val isLengthValid: Boolean = false,
    val hasAllowedCharacters: Boolean = false,
    val hasNoSpecialCharacters: Boolean = false,
    val isLoading: Boolean = false,
    val createdJoinCode: String? = null,
    val errorMessage: String? = null,
)
