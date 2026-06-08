package com.woowacourse.runpamine.domain.auth

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val user: AuthUser,
)

data class AuthUser(
    val id: String,
    val email: String?,
)
