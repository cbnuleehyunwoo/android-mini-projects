package com.woowacourse.runpamine.domain.auth

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeSession(): Flow<AuthSession?>

    suspend fun getCurrentSession(): AuthSession?

    suspend fun signInWithGoogleIdToken(
        idToken: String,
        nonce: String?,
    ): AuthSession

    suspend fun signOut()
}
