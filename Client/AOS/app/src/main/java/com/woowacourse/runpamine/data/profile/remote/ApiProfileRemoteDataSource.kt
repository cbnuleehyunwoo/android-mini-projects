package com.woowacourse.runpamine.data.profile.remote

import com.woowacourse.runpamine.domain.profile.HomeState
import com.woowacourse.runpamine.domain.profile.TeamSummary
import com.woowacourse.runpamine.domain.profile.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ApiProfileRemoteDataSource(
    baseUrl: String,
) : ProfileRemoteDataSource {
    private val apiBaseUrl = baseUrl.toApiBaseUrl()

    override suspend fun getHomeState(accessToken: String): HomeState =
        withContext(Dispatchers.IO) {
            val response =
                request(
                    path = "/home",
                    method = "GET",
                    accessToken = accessToken,
                )
            val data = response.getJSONObject("data")
            HomeState(
                profile = data.optJSONObject("profile")?.toUserProfile(),
                team = data.optJSONObject("team")?.toTeamSummary(),
            )
        }

    override suspend fun getMyProfile(accessToken: String): UserProfile? = getHomeState(accessToken).profile

    override suspend fun createProfile(
        accessToken: String,
        request: CreateProfileRequest,
    ): UserProfile =
        withContext(Dispatchers.IO) {
            val body =
                JSONObject()
                    .put("nickname", request.nickname)
                    .put("avatarKey", request.avatarKey)

            val response =
                request(
                    path = "/profile",
                    method = "POST",
                    accessToken = accessToken,
                    body = body,
                )
            response.getJSONObject("data").toUserProfile()
        }

    private fun request(
        path: String,
        method: String,
        accessToken: String,
        body: JSONObject? = null,
    ): JSONObject {
        val connection = URL("$apiBaseUrl$path").openConnection() as HttpURLConnection
        return connection.useJsonRequest(method, accessToken, body)
    }
}

private fun HttpURLConnection.useJsonRequest(
    method: String,
    accessToken: String,
    body: JSONObject?,
): JSONObject =
    try {
        requestMethod = method
        connectTimeout = CONNECT_TIMEOUT_MILLIS
        readTimeout = READ_TIMEOUT_MILLIS
        setRequestProperty("Authorization", "Bearer $accessToken")
        setRequestProperty("Accept", "application/json")
        if (body != null) {
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            outputStream.bufferedWriter().use { writer ->
                writer.write(body.toString())
            }
        }

        val responseText =
            if (responseCode in 200..299) {
                inputStream.bufferedReader().use { it.readText() }
            } else {
                val errorText = errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                throw IllegalStateException(errorText.toApiErrorMessage(responseCode))
            }

        JSONObject(responseText)
    } finally {
        disconnect()
    }

private fun JSONObject.toUserProfile(): UserProfile =
    UserProfile(
        id = getString("id"),
        nickname = getString("nickname"),
        avatarKey = optString("avatarKey").takeIf { it.isNotBlank() },
        teamId = optString("teamId").takeIf { it.isNotBlank() },
    )

private fun JSONObject.toTeamSummary(): TeamSummary =
    TeamSummary(
        id = getString("id"),
        name = getString("name"),
        joinCode = optString("joinCode").takeIf { it.isNotBlank() },
        ownerId = optString("ownerId").takeIf { it.isNotBlank() },
        memberCount = optInt("memberCount", 0),
        isOwner = optBoolean("isOwner", false),
    )

private fun String.toApiErrorMessage(responseCode: Int): String =
    runCatching {
        JSONObject(this).getJSONObject("error").getString("message")
    }.getOrDefault("프로필 요청에 실패했어요. ($responseCode)")

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
