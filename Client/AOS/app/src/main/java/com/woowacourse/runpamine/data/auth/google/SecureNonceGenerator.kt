package com.woowacourse.runpamine.data.auth.google

import java.security.SecureRandom

class SecureNonceGenerator {
    private val secureRandom = SecureRandom()

    fun generate(): String =
        buildString(NONCE_LENGTH) {
            repeat(NONCE_LENGTH) {
                append(NONCE_CHARACTERS[secureRandom.nextInt(NONCE_CHARACTERS.length)])
            }
        }

    private companion object {
        const val NONCE_LENGTH = 32
        const val NONCE_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    }
}
