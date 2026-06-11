package com.woowacourse.runpamine.data.auth.repository

import com.woowacourse.runpamine.data.auth.remote.AuthRemoteDataSource
import com.woowacourse.runpamine.domain.auth.AuthRepository
import com.woowacourse.runpamine.domain.auth.AuthSession
import kotlinx.coroutines.flow.Flow

class DefaultAuthRepository(
    private val remoteDataSource: AuthRemoteDataSource,
) : AuthRepository {
    override fun observeSession(): Flow<AuthSession?> = remoteDataSource.observeSession()

    override suspend fun loadSessionFromStorage(): AuthSession? = remoteDataSource.loadSessionFromStorage()

    override suspend fun getCurrentSession(): AuthSession? = remoteDataSource.getCurrentSession()

    override suspend fun signInWithGoogleIdToken(
        idToken: String,
        nonce: String?,
    ): AuthSession =
        remoteDataSource.signInWithGoogleIdToken(
            idToken = idToken,
            nonce = nonce,
        )

    override suspend fun signOut() {
        remoteDataSource.signOut()
    }

    override suspend fun deleteAccount() {
        remoteDataSource.deleteAccount()
    }
}
