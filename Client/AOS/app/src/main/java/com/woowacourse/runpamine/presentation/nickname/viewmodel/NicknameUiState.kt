package com.woowacourse.runpamine.presentation.nickname.viewmodel

data class NicknameUiState(
    val nickname: String = "",
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false,
    val errorMessage: String? = null,
) {
    val isValid: Boolean
        get() = nickname.length in NICKNAME_MIN_LENGTH..NICKNAME_MAX_LENGTH && NICKNAME_REGEX.matches(nickname)
}

const val NICKNAME_MIN_LENGTH = 2
const val NICKNAME_MAX_LENGTH = 10
val NICKNAME_REGEX = Regex("^[가-힣a-zA-Z0-9]*$")
