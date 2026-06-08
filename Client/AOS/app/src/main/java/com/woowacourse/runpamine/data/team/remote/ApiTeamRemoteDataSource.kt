package com.woowacourse.runpamine.data.team.remote

import com.woowacourse.runpamine.domain.team.Team
import com.woowacourse.runpamine.domain.team.TeamDailySummary
import com.woowacourse.runpamine.domain.team.TeamMemberSummary
import com.woowacourse.runpamine.domain.team.TeamRunSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ApiTeamRemoteDataSource(
    baseUrl: String,
) : TeamRemoteDataSource {
    private val apiBaseUrl = baseUrl.toApiBaseUrl()

    override suspend fun createTeam(
        accessToken: String,
        name: String,
    ): Team =
        withContext(Dispatchers.IO) {
            val response =
                request(
                    path = "/teams",
                    method = "POST",
                    accessToken = accessToken,
                    body = JSONObject().put("name", name),
                )
            response.getJSONObject("data").toTeam()
        }

    override suspend fun joinTeam(
        accessToken: String,
        joinCode: String,
    ): Team =
        withContext(Dispatchers.IO) {
            val response =
                request(
                    path = "/teams/join",
                    method = "POST",
                    accessToken = accessToken,
                    body = JSONObject().put("joinCode", joinCode),
                )
            response.getJSONObject("data").toTeam()
        }

    override suspend fun getMyTeam(accessToken: String): Team =
        withContext(Dispatchers.IO) {
            val response =
                request(
                    path = "/teams/me",
                    method = "GET",
                    accessToken = accessToken,
                )
            response.getJSONObject("data").toTeam()
        }

    override suspend fun getMyTeamMembers(accessToken: String): List<TeamMemberSummary> =
        withContext(Dispatchers.IO) {
            val response =
                request(
                    path = "/teams/me/members",
                    method = "GET",
                    accessToken = accessToken,
                )
            val members = response.getJSONObject("data").getJSONArray("members")
            List(members.length()) { index ->
                members.getJSONObject(index).toTeamMemberSummary()
            }
        }

    override suspend fun getMyTeamDailySummary(accessToken: String): TeamDailySummary =
        withContext(Dispatchers.IO) {
            val response =
                request(
                    path = "/teams/me/runs",
                    method = "GET",
                    accessToken = accessToken,
                )
            response.getJSONObject("data").toTeamDailySummary()
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

private fun JSONObject.toTeam(): Team =
    Team(
        id = getString("id"),
        name = getString("name"),
        joinCode = optString("joinCode").orEmpty(),
        ownerId = optString("ownerId").orEmpty(),
        memberCount = optInt("memberCount", 0),
        isOwner = optBoolean("isOwner", false),
    )

private fun JSONObject.toTeamMemberSummary(): TeamMemberSummary =
    TeamMemberSummary(
        id = getString("id"),
        nickname = getString("nickname"),
        avatarKey = optString("avatarKey").takeIf { it.isNotBlank() },
    )

private fun JSONObject.toTeamDailySummary(): TeamDailySummary {
    val members = getJSONArray("members")
    return TeamDailySummary(
        team = getJSONObject("team").toTeam(),
        date = getString("date"),
        teamTotalDistanceMeters = getInt("teamTotalDistanceMeters"),
        completedMemberCount = getInt("completedMemberCount"),
        totalMemberCount = getInt("totalMemberCount"),
        members =
            List(members.length()) { index ->
                members.getJSONObject(index).toTeamRunSummary()
            },
    )
}

private fun JSONObject.toTeamRunSummary(): TeamRunSummary =
    TeamRunSummary(
        userId = getString("userId"),
        nickname = getString("nickname"),
        distanceMeters = getInt("distanceMeters"),
        durationSeconds = getInt("durationSeconds"),
        averagePaceSecondsPerKm = getInt("averagePaceSecondsPerKm"),
        calories = getInt("calories"),
        completed = getBoolean("completed"),
    )

private fun String.toApiErrorMessage(responseCode: Int): String =
    runCatching {
        JSONObject(this).getJSONObject("error").getString("message")
    }.getOrDefault("팀 요청에 실패했어요. ($responseCode)")

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
