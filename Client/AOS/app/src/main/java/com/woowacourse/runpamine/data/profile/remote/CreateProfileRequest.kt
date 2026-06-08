package com.woowacourse.runpamine.data.profile.remote

data class CreateProfileRequest(
    val nickname: String,
    val avatarKey: String = DEFAULT_AVATAR_KEY,
) {
    companion object {
        const val DEFAULT_AVATAR_KEY = "runner_default"
    }
}
