package com.woowacourse.runpamine.data.auth.remote

import com.woowacourse.runpamine.domain.auth.AuthSession
import kotlinx.coroutines.flow.Flow

interface AuthRemoteDataSource {
    fun observeSession(): Flow<AuthSession?>

    suspend fun loadSessionFromStorage(): AuthSession?

    suspend fun getCurrentSession(): AuthSession?

    suspend fun signInWithGoogleIdToken(
        idToken: String,
        nonce: String?,
    ): AuthSession

    suspend fun signOut()

    suspend fun deleteAccount()
}
