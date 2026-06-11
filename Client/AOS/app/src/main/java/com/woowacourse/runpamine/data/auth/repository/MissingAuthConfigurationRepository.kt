package com.woowacourse.runpamine.data.auth.repository

import com.woowacourse.runpamine.domain.auth.AuthRepository
import com.woowacourse.runpamine.domain.auth.AuthSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MissingAuthConfigurationRepository(
    private val missingKeys: List<String>,
) : AuthRepository {
    override fun observeSession(): Flow<AuthSession?> = flowOf(null)

    override suspend fun loadSessionFromStorage(): AuthSession? = null

    override suspend fun getCurrentSession(): AuthSession? = null

    override suspend fun signInWithGoogleIdToken(
        idToken: String,
        nonce: String?,
    ): AuthSession = throw IllegalStateException("${missingKeys.joinToString()} 설정이 local.properties에 필요해요.")

    override suspend fun signOut() = Unit

    override suspend fun deleteAccount() = Unit
}
