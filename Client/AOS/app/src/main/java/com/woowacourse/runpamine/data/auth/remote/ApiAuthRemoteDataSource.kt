package com.woowacourse.runpamine.data.auth.remote

import com.woowacourse.runpamine.data.auth.storage.EncryptedAuthSessionStore
import com.woowacourse.runpamine.data.network.toRunpamineApiBaseUrl
import com.woowacourse.runpamine.domain.auth.AuthSession
import com.woowacourse.runpamine.domain.auth.AuthUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64

class ApiAuthRemoteDataSource(
    baseUrl: String,
    private val sessionStore: EncryptedAuthSessionStore,
) : AuthRemoteDataSource {
    private val apiBaseUrl = baseUrl.toRunpamineApiBaseUrl()
    private val refreshMutex = Mutex()

    override fun observeSession(): Flow<AuthSession?> = sessionStore.observe()

    override suspend fun loadSessionFromStorage(): AuthSession? = sessionStore.current()?.let { ensureFreshSession(it) }

    override suspend fun getCurrentSession(): AuthSession? = sessionStore.current()?.let { ensureFreshSession(it) }

    override suspend fun signInWithGoogleIdToken(
        idToken: String,
        nonce: String?,
    ): AuthSession {
        val payload = JSONObject().put("idToken", idToken)
        if (!nonce.isNullOrBlank()) {
            payload.put("nonce", nonce)
        }
        val data = request(path = "/auth/google", method = "POST", body = payload)
        val user = data.getJSONObject("user")
        val session =
            AuthSession(
                accessToken = data.getString("accessToken"),
                refreshToken = data.getString("refreshToken"),
                user =
                    AuthUser(
                        id = user.getString("id"),
                        email = user.optString("email").takeIf { it.isNotBlank() },
                    ),
            )
        sessionStore.save(session)
        return session
    }

    override suspend fun signOut() {
        val session = sessionStore.current() ?: return
        try {
            val freshSession = ensureFreshSession(session)
            request(
                path = "/auth/logout",
                method = "POST",
                accessToken = freshSession.accessToken,
            )
        } catch (exception: ApiAuthException) {
            if (exception.statusCode != HttpURLConnection.HTTP_UNAUTHORIZED) throw exception
        }
        sessionStore.clear()
    }

    override suspend fun deleteAccount() {
        val session = requireNotNull(sessionStore.current()) { "로그인이 필요해요." }
        val freshSession = ensureFreshSession(session)
        val data =
            request(
                path = "/account/me",
                method = "DELETE",
                accessToken = freshSession.accessToken,
            )
        check(data.getBoolean("deleted")) { "회원 탈퇴에 실패했어요." }
        sessionStore.clear()
    }

    private suspend fun ensureFreshSession(session: AuthSession): AuthSession {
        if (!session.accessToken.isExpiringSoon()) return session

        return refreshMutex.withLock {
            val latest = requireNotNull(sessionStore.current()) { "로그인이 필요해요." }
            if (latest.refreshToken != session.refreshToken || !latest.accessToken.isExpiringSoon()) {
                return@withLock latest
            }

            val data =
                try {
                    request(
                        path = "/auth/refresh",
                        method = "POST",
                        body = JSONObject().put("refreshToken", latest.refreshToken),
                    )
                } catch (exception: ApiAuthException) {
                    if (exception.statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        sessionStore.clear()
                    }
                    throw exception
                }
            val refreshed =
                latest.copy(
                    accessToken = data.getString("accessToken"),
                    refreshToken = data.getString("refreshToken"),
                )
            sessionStore.save(refreshed)
            refreshed
        }
    }

    private suspend fun request(
        path: String,
        method: String,
        body: JSONObject? = null,
        accessToken: String? = null,
    ): JSONObject =
        withContext(Dispatchers.IO) {
            val connection = URL("$apiBaseUrl$path").openConnection() as HttpURLConnection
            try {
                connection.requestMethod = method
                connection.connectTimeout = CONNECT_TIMEOUT_MILLIS
                connection.readTimeout = READ_TIMEOUT_MILLIS
                connection.setRequestProperty("Accept", "application/json")
                if (accessToken != null) {
                    connection.setRequestProperty("Authorization", "Bearer $accessToken")
                }
                if (body != null) {
                    connection.doOutput = true
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                        writer.write(body.toString())
                    }
                }

                val responseText =
                    if (connection.responseCode in 200..299) {
                        connection.inputStream.bufferedReader().use { it.readText() }
                    } else {
                        connection.errorStream
                            ?.bufferedReader()
                            ?.use { it.readText() }
                            .orEmpty()
                    }
                if (connection.responseCode !in 200..299) {
                    throw ApiAuthException(
                        statusCode = connection.responseCode,
                        message = responseText.toApiErrorMessage(connection.responseCode),
                    )
                }
                JSONObject(responseText).getJSONObject("data")
            } finally {
                connection.disconnect()
            }
        }
}

private class ApiAuthException(
    val statusCode: Int,
    message: String,
) : IllegalStateException(message)

private fun String.toApiErrorMessage(responseCode: Int): String =
    runCatching { JSONObject(this).getJSONObject("error").getString("message") }
        .getOrDefault("요청에 실패했어요. ($responseCode)")

private fun String.isExpiringSoon(): Boolean {
    val expiresAtMillis =
        runCatching {
            val payload = split('.')[1]
            val decoded = Base64.getUrlDecoder().decode(payload)
            JSONObject(String(decoded, Charsets.UTF_8)).getLong("exp") * 1_000L
        }.getOrDefault(0L)
    return expiresAtMillis <= System.currentTimeMillis() + TOKEN_REFRESH_WINDOW_MILLIS
}

private const val CONNECT_TIMEOUT_MILLIS = 10_000
private const val READ_TIMEOUT_MILLIS = 10_000
private const val TOKEN_REFRESH_WINDOW_MILLIS = 60_000L
