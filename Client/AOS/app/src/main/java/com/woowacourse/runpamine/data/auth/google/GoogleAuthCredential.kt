package com.woowacourse.runpamine.data.auth.google

data class GoogleAuthCredential(
    val idToken: String,
    val nonce: String?,
)
