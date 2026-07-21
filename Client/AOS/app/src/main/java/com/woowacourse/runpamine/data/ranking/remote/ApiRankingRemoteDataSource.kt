package com.woowacourse.runpamine.data.ranking.remote

import com.woowacourse.runpamine.domain.ranking.MyRankingSummary
import com.woowacourse.runpamine.domain.ranking.RankingMetric
import com.woowacourse.runpamine.domain.ranking.RankingSeason
import com.woowacourse.runpamine.domain.ranking.TeamRanking
import com.woowacourse.runpamine.domain.ranking.UserRanking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ApiRankingRemoteDataSource(
    baseUrl: String,
) : RankingRemoteDataSource {
    private val apiBaseUrl = baseUrl.toApiBaseUrl()

    override suspend fun getTeamRankings(
        accessToken: String,
        metric: RankingMetric,
    ): List<TeamRanking> =
        withContext(Dispatchers.IO) {
            val response =
                request(
                    path = "/rankings/teams/${metric.pathSegment}",
                    method = "GET",
                    accessToken = accessToken,
                )
            val rankings = response.getJSONObject("data").getJSONArray("rankings")
            List(rankings.length()) { index ->
                rankings.getJSONObject(index).toTeamRanking()
            }
        }

    override suspend fun getUserRankings(
        accessToken: String,
        metric: RankingMetric,
    ): List<UserRanking> =
        withContext(Dispatchers.IO) {
            val response =
                request(
                    path = "/rankings/users/${metric.pathSegment}",
                    method = "GET",
                    accessToken = accessToken,
                )
            val rankings = response.getJSONObject("data").getJSONArray("rankings")
            List(rankings.length()) { index ->
                rankings.getJSONObject(index).toUserRanking()
            }
        }

    override suspend fun getMyRankingSummary(accessToken: String): MyRankingSummary =
        withContext(Dispatchers.IO) {
            val response =
                request(
                    path = "/rankings/me",
                    method = "GET",
                    accessToken = accessToken,
                )
            response.getJSONObject("data").toMyRankingSummary()
        }

    private fun request(
        path: String,
        method: String,
        accessToken: String,
    ): JSONObject {
        val connection = URL("$apiBaseUrl$path").openConnection() as HttpURLConnection
        return connection.useJsonRequest(method, accessToken)
    }
}

private fun HttpURLConnection.useJsonRequest(
    method: String,
    accessToken: String,
): JSONObject =
    try {
        requestMethod = method
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

        JSONObject(responseText)
    } catch (throwable: Throwable) {
        throw throwable
    } finally {
        disconnect()
    }

private val RankingMetric.pathSegment: String
    get() =
        when (this) {
            RankingMetric.DISTANCE -> "distance"
            RankingMetric.PACE -> "pace"
            RankingMetric.CONSISTENCY -> "count"
        }

private fun JSONObject.toTeamRanking(): TeamRanking =
    TeamRanking(
        rank = getInt("rank"),
        teamId = getString("teamId"),
        teamName = getString("teamName"),
        distanceMeters = getInt("distanceMeters"),
        durationSeconds = optLong("durationSeconds", 0L),
        averagePaceSecondsPerKm = optInt("averagePaceSecondsPerKm", 0),
        runCount = optInt("runCount", 0),
        totalActiveDays = optInt("totalActiveDays", 0),
        averageActiveDays = optDouble("averageActiveDays", 0.0),
    )

private fun JSONObject.toUserRanking(): UserRanking =
    UserRanking(
        rank = getInt("rank"),
        userId = getString("userId"),
        nickname = getString("nickname"),
        avatarKey = optString("avatarKey").takeIf { it.isNotBlank() },
        teamId = optString("teamId").takeIf { it.isNotBlank() },
        teamName = optString("teamName").takeIf { it.isNotBlank() },
        distanceMeters = getInt("distanceMeters"),
        durationSeconds = optLong("durationSeconds", 0L),
        averagePaceSecondsPerKm = optInt("averagePaceSecondsPerKm", 0),
        runCount = optInt("runCount", 0),
        activeDays = optInt("activeDays", 0),
        elapsedDays = optInt("elapsedDays", 0),
        consistencyRate = optInt("consistencyRate", 0),
    )

private fun JSONObject.toMyRankingSummary(): MyRankingSummary =
    MyRankingSummary(
        season = getJSONObject("season").toRankingSeason(),
        eligible = getBoolean("eligible"),
        requiredDistanceMeters = getInt("requiredDistanceMeters"),
        distanceMeters = getInt("distanceMeters"),
        remainingDistanceMeters = getInt("remainingDistanceMeters"),
        durationSeconds = optLong("durationSeconds", 0L),
        averagePaceSecondsPerKm = optInt("averagePaceSecondsPerKm", 0),
        runCount = optInt("runCount", 0),
        activeDays = optInt("activeDays", 0),
        consistencyRate = optInt("consistencyRate", 0),
        distanceRank = optNullableInt("distanceRank"),
        distanceTopPercent = optNullableDouble("distanceTopPercent"),
        paceRank = optNullableInt("paceRank"),
        paceTopPercent = optNullableDouble("paceTopPercent"),
        consistencyRank = optNullableInt("consistencyRank"),
        consistencyTopPercent = optNullableDouble("consistencyTopPercent"),
    )

private fun JSONObject.toRankingSeason(): RankingSeason =
    RankingSeason(
        id = getString("id"),
        name = getString("name"),
        year = getInt("year"),
        month = getInt("month"),
        elapsedDays = optInt("elapsedDays", 0),
    )

private fun JSONObject.optNullableInt(name: String): Int? = if (isNull(name)) null else optInt(name)

private fun JSONObject.optNullableDouble(name: String): Double? = if (isNull(name)) null else optDouble(name)

private fun String.toApiErrorMessage(responseCode: Int): String =
    runCatching {
        JSONObject(this).getJSONObject("error").getString("message")
    }.getOrDefault("랭킹 요청에 실패했어요. ($responseCode)")

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
