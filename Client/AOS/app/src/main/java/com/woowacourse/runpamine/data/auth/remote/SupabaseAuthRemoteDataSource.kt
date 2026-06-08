package com.woowacourse.runpamine.data.auth.remote

import com.woowacourse.runpamine.domain.auth.AuthSession
import com.woowacourse.runpamine.domain.auth.AuthUser
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SupabaseAuthRemoteDataSource(
    private val supabaseClient: SupabaseClient,
) : AuthRemoteDataSource {
    override fun observeSession(): Flow<AuthSession?> =
        supabaseClient.auth.sessionStatus.map { status ->
            when (status) {
                is SessionStatus.Authenticated -> status.session.toDomain()
                else -> null
            }
        }

    override suspend fun getCurrentSession(): AuthSession? = supabaseClient.auth.currentSessionOrNull()?.toDomain()

    override suspend fun signInWithGoogleIdToken(
        idToken: String,
        nonce: String?,
    ): AuthSession {
        supabaseClient.auth.signInWith(IDToken) {
            this.idToken = idToken
            provider = Google
            this.nonce = nonce
        }

        return requireNotNull(supabaseClient.auth.currentSessionOrNull()) {
            "Supabase session was not created."
        }.toDomain()
    }

    override suspend fun signOut() {
        supabaseClient.auth.signOut()
    }
}

private fun io.github.jan.supabase.auth.user.UserSession.toDomain(): AuthSession =
    AuthSession(
        accessToken = accessToken,
        refreshToken = refreshToken,
        user =
            AuthUser(
                id = user?.id.orEmpty(),
                email = user?.email,
            ),
    )
