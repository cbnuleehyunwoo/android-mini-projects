package com.woowacourse.runpamine.data.team.remote

import com.woowacourse.runpamine.data.network.toRunpamineApiBaseUrl
import com.woowacourse.runpamine.domain.team.LeaveTeamResult
import com.woowacourse.runpamine.domain.team.Team
import com.woowacourse.runpamine.domain.team.TeamDailySummary
import com.woowacourse.runpamine.domain.team.TeamMemberStats
import com.woowacourse.runpamine.domain.team.TeamMemberSummary
import com.woowacourse.runpamine.domain.team.TeamRunSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

class ApiTeamRemoteDataSource(
    baseUrl: String,
) : TeamRemoteDataSource {
    private val apiBaseUrl = baseUrl.toRunpamineApiBaseUrl()

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
            val data = response.optJSONObject("data")
            val members =
                when {
                    data?.has("members") == true -> data.getJSONArray("members")
                    response.has("members") -> response.getJSONArray("members")
                    else -> response.getJSONArray("data")
                }
            List(members.length()) { index ->
                members.getJSONObject(index).toTeamMemberSummary()
            }
        }

    override suspend fun getMyTeamStats(accessToken: String): List<TeamMemberStats> =
        withContext(Dispatchers.IO) {
            val response =
                request(
                    path = "/teams/me/stats?period=$ALL_TIME_PERIOD",
                    method = "GET",
                    accessToken = accessToken,
                )
            val members = response.getJSONObject("data").getJSONArray("members")
            List(members.length()) { index ->
                members.getJSONObject(index).toTeamMemberStats()
            }
        }

    override suspend fun getMyTeamDailySummary(
        accessToken: String,
        date: LocalDate,
    ): TeamDailySummary =
        withContext(Dispatchers.IO) {
            val response =
                request(
                    path = "/teams/me/runs?date=$date",
                    method = "GET",
                    accessToken = accessToken,
                )
            response.getJSONObject("data").toTeamDailySummary()
        }

    override suspend fun leaveTeam(accessToken: String): LeaveTeamResult =
        withContext(Dispatchers.IO) {
            val response =
                request(
                    path = "/teams/me",
                    method = "DELETE",
                    accessToken = accessToken,
                )
            response.getJSONObject("data").toLeaveTeamResult()
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
    } catch (throwable: Throwable) {
        throw throwable
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

private fun JSONObject.toLeaveTeamResult(): LeaveTeamResult =
    LeaveTeamResult(
        left = getBoolean("left"),
        teamDeleted = getBoolean("teamDeleted"),
        newOwnerId = optString("newOwnerId").takeIf { it.isNotBlank() },
    )

private fun JSONObject.toTeamMemberSummary(): TeamMemberSummary =
    TeamMemberSummary(
        id =
            optString("id")
                .ifBlank { optString("userId") }
                .ifBlank { optString("profileId") },
        nickname =
            optString("nickname")
                .ifBlank { optJSONObject("profile")?.optString("nickname").orEmpty() },
        avatarKey = optString("avatarKey").takeIf { it.isNotBlank() },
    )

private fun JSONObject.toTeamMemberStats(): TeamMemberStats =
    TeamMemberStats(
        id = getString("id"),
        nickname = getString("nickname"),
        avatarKey = optString("avatarKey").takeIf { it.isNotBlank() },
        teamJoinedAt = optString("teamJoinedAt"),
        distanceMeters = optInt("distanceMeters"),
        durationSeconds = optInt("durationSeconds"),
        calories = optInt("calories"),
        runCount = optInt("runCount"),
        activeDays = optInt("activeDays"),
        averagePaceSecondsPerKm =
            if (has("averagePaceSecondsPerKm") && !isNull("averagePaceSecondsPerKm")) {
                getInt("averagePaceSecondsPerKm")
            } else {
                null
            },
        recentRunDays = optInt("recentRunDays"),
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
        userId = optString("userId").ifBlank { optString("id") },
        nickname = getString("nickname"),
        teamJoinedAt = optString("teamJoinedAt"),
        distanceMeters = getInt("distanceMeters"),
        durationSeconds = getInt("durationSeconds"),
        averagePaceSecondsPerKm = getInt("averagePaceSecondsPerKm"),
        calories = getInt("calories"),
        completed = getBoolean("completed"),
        totalDistanceMeters = optInt("totalDistanceMeters"),
        totalDurationSeconds = optInt("totalDurationSeconds"),
        totalRunCount = optInt("totalRunCount"),
        totalAveragePaceSecondsPerKm =
            if (has("totalAveragePaceSecondsPerKm") && !isNull("totalAveragePaceSecondsPerKm")) {
                getInt("totalAveragePaceSecondsPerKm")
            } else {
                null
            },
    )

private fun String.toApiErrorMessage(responseCode: Int): String =
    runCatching {
        val message = JSONObject(this).getJSONObject("error").getString("message")
        if (responseCode == HttpURLConnection.HTTP_CONFLICT && message == TEAM_NAME_ALREADY_EXISTS) {
            DUPLICATE_TEAM_NAME_MESSAGE
        } else {
            message
        }
    }.getOrDefault("팀 요청에 실패했어요. ($responseCode)")

private const val CONNECT_TIMEOUT_MILLIS = 10_000
private const val READ_TIMEOUT_MILLIS = 10_000
private const val TEAM_NAME_ALREADY_EXISTS = "Team name already exists"
private const val DUPLICATE_TEAM_NAME_MESSAGE = "중복된 팀 이름입니다."
private const val ALL_TIME_PERIOD = "all"
private const val TAG = "TeamApi"
