package com.woowacourse.runpamine.domain.profile

data class UserProfile(
    val id: String,
    val nickname: String,
    val avatarKey: String?,
    val teamId: String?,
)
