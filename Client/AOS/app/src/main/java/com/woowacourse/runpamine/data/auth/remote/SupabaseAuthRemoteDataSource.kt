package com.woowacourse.runpamine.data.auth.remote

import com.woowacourse.runpamine.domain.auth.AuthSession
import com.woowacourse.runpamine.domain.auth.AuthUser
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class SupabaseAuthRemoteDataSource(
    private val supabaseClient: SupabaseClient,
    baseUrl: String,
) : AuthRemoteDataSource {
    private val apiBaseUrl = baseUrl.toApiBaseUrl()

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
        val accessToken = supabaseClient.auth.currentSessionOrNull()?.accessToken
        if (accessToken != null) {
            requestLogout(accessToken)
        }
        supabaseClient.auth.signOut()
    }

    private suspend fun requestLogout(accessToken: String) {
        withContext(Dispatchers.IO) {
            val connection = URL("$apiBaseUrl/auth/logout").openConnection() as HttpURLConnection
            connection.useLogoutRequest(accessToken)
        }
    }
}

private fun HttpURLConnection.useLogoutRequest(accessToken: String) {
    try {
        requestMethod = "POST"
        connectTimeout = CONNECT_TIMEOUT_MILLIS
        readTimeout = READ_TIMEOUT_MILLIS
        setRequestProperty("Authorization", "Bearer $accessToken")
        setRequestProperty("Accept", "application/json")

        val responseText =
            if (responseCode in 200..299) {
                inputStream.bufferedReader().use { it.readText() }
            } else {
                val errorText = errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                throw IllegalStateException(errorText.toApiErrorMessage(responseCode))
            }

        val success = JSONObject(responseText).getJSONObject("data").getBoolean("success")
        check(success) { "로그아웃에 실패했어요." }
    } finally {
        disconnect()
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

private fun String.toApiErrorMessage(responseCode: Int): String =
    runCatching {
        JSONObject(this).getJSONObject("error").getString("message")
    }.getOrDefault("로그아웃에 실패했어요. ($responseCode)")

private fun String.toApiBaseUrl(): String {
    val normalized = trim().trimEnd('/')
    return if (normalized.endsWith(".supabase.co")) {
        "$normalized/functions/v1/api"
    } else {
        normalized
    }
}

private const val CONNECT_TIMEOUT_MILLIS = 10_000
private const val READ_TIMEOUT_MILLIS = 10_000
