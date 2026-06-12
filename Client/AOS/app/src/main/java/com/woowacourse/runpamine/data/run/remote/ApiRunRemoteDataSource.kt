package com.woowacourse.runpamine.data.run.remote

import android.util.Log
import com.woowacourse.runpamine.domain.run.RunDaySummary
import com.woowacourse.runpamine.domain.run.RunPeriodSummary
import com.woowacourse.runpamine.domain.run.RunPoint
import com.woowacourse.runpamine.domain.run.RunResult
import com.woowacourse.runpamine.domain.run.RunSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class ApiRunRemoteDataSource(
    baseUrl: String,
) : RunRemoteDataSource {
    private val apiBaseUrl = baseUrl.toApiBaseUrl()

    override suspend fun createRun(
        accessToken: String,
        session: RunSession,
        points: List<RunPoint>,
    ): RunResult =
        withContext(Dispatchers.IO) {
            val response =
                request(
                    path = "/runs",
                    method = "POST",
                    accessToken = accessToken,
                    body = session.toCreateRunRequest(points),
                )
            response.getJSONObject("data").toRunResult()
        }

    override suspend fun getWeeklyRuns(
        accessToken: String,
        anchorDate: LocalDate,
    ): RunPeriodSummary =
        withContext(Dispatchers.IO) {
            val response =
                request(
                    path = "/runs/me/week",
                    query = mapOf("date" to anchorDate.toString()),
                    method = "GET",
                    accessToken = accessToken,
                )
            response.getJSONObject("data").toRunPeriodSummary()
        }

    override suspend fun getMonthlyRuns(
        accessToken: String,
        yearMonth: YearMonth,
    ): RunPeriodSummary =
        withContext(Dispatchers.IO) {
            val response =
                request(
                    path = "/runs/me/month",
                    query =
                        mapOf(
                            "year" to yearMonth.year.toString(),
                            "month" to yearMonth.monthValue.toString(),
                        ),
                    method = "GET",
                    accessToken = accessToken,
                )
            response.getJSONObject("data").toRunPeriodSummary()
        }

    override suspend fun getRunDetail(
        accessToken: String,
        runId: String,
    ): RunSession =
        withContext(Dispatchers.IO) {
            val response =
                request(
                    path = "/runs/$runId",
                    method = "GET",
                    accessToken = accessToken,
                )
            response.getJSONObject("data").toRunSession()
        }

    private fun request(
        path: String,
        method: String,
        accessToken: String,
        query: Map<String, String> = emptyMap(),
        body: JSONObject? = null,
    ): JSONObject {
        val url = "$apiBaseUrl$path${query.toQueryString()}"
        val connection = URL(url).openConnection() as HttpURLConnection
        return connection.useJsonRequest(method, accessToken, body)
    }
}

private fun HttpURLConnection.useJsonRequest(
    method: String,
    accessToken: String,
    body: JSONObject?,
): JSONObject =
    try {
        val requestUrl = url.toString()
        Log.d(TAG, "HTTP $method $requestUrl")

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
                inputStream.bufferedReader().use { it.readText() }.also { text ->
                    Log.d(TAG, "Response $responseCode $method $requestUrl: $text")
                }
            } else {
                val errorText = errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                Log.w(TAG, "Error $responseCode $method $requestUrl: $errorText")
                throw IllegalStateException(errorText.toApiErrorMessage(responseCode))
            }

        JSONObject(responseText)
    } catch (throwable: Throwable) {
        Log.e(TAG, "HTTP $method $url failed.", throwable)
        throw throwable
    } finally {
        disconnect()
    }

private fun RunSession.toCreateRunRequest(points: List<RunPoint>): JSONObject =
    JSONObject()
        .put("startedAt", startedAt.toApiDateTimeString())
        .put("endedAt", requireNotNull(endedAt).toApiDateTimeString())
        .put("distanceMeters", distanceMeters)
        .put("durationSeconds", durationSeconds)
        .put("calories", calories)
        .put(
            "points",
            JSONArray(
                points.map { point ->
                    JSONObject()
                        .put("sequence", point.sequence)
                        .put("latitude", point.latitude)
                        .put("longitude", point.longitude)
                        .put("recordedAt", point.recordedAt.toApiDateTimeString())
                },
            ),
        )

private fun JSONObject.toRunResult(): RunResult =
    RunResult(
        id = getString("id"),
        distanceMeters = getInt("distanceMeters"),
        durationSeconds = getLong("durationSeconds"),
        averagePaceSecondsPerKm = optInt("averagePaceSecondsPerKm", 0),
        calories = getInt("calories"),
    )

private fun JSONObject.toRunPeriodSummary(): RunPeriodSummary {
    val days = getJSONArray("days")
    val runs = getJSONArray("runs")
    return RunPeriodSummary(
        totalDistanceMeters = getInt("totalDistanceMeters"),
        days =
            List(days.length()) { index ->
                days.getJSONObject(index).toRunDaySummary()
            },
        runs =
            List(runs.length()) { index ->
                runs.getJSONObject(index).toRunSession()
            }.sortedByDescending { it.startedAt },
    )
}

private fun JSONObject.toRunDaySummary(): RunDaySummary =
    RunDaySummary(
        date = LocalDate.parse(getString("date")),
        distanceMeters = getInt("distanceMeters"),
        hasRun = getBoolean("hasRun"),
    )

private fun JSONObject.toRunSession(): RunSession =
    RunSession(
        id = getString("id"),
        startedAt = parseApiInstant(getString("startedAt")),
        endedAt = parseApiInstant(getString("endedAt")),
        distanceMeters = getInt("distanceMeters"),
        durationSeconds = getLong("durationSeconds"),
        averagePaceSecondsPerKm = optInt("averagePaceSecondsPerKm", 0),
        calories = getInt("calories"),
        routePoints = optJSONArray("points").toRunPoints(id = getString("id")),
    )

private fun JSONArray?.toRunPoints(id: String): List<RunPoint> {
    if (this == null) return emptyList()
    return List(length()) { index ->
        getJSONObject(index)
    }.map { point ->
        RunPoint(
            sessionId = id,
            sequence = point.getInt("sequence"),
            latitude = point.getDouble("latitude"),
            longitude = point.getDouble("longitude"),
            recordedAt = parseApiInstant(point.getString("recordedAt")),
        )
    }.sortedBy { it.sequence }
}

private fun Instant.toApiDateTimeString(): String = DateTimeFormatter.ISO_INSTANT.format(this)

private fun parseApiInstant(value: String): Instant =
    runCatching { Instant.parse(value) }
        .getOrElse {
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(value, Instant::from)
        }

private fun Map<String, String>.toQueryString(): String {
    if (isEmpty()) return ""
    return entries.joinToString(prefix = "?", separator = "&") { (key, value) ->
        "${key.urlEncode()}=${value.urlEncode()}"
    }
}

private fun String.urlEncode(): String = URLEncoder.encode(this, Charsets.UTF_8.name())

private fun String.toApiErrorMessage(responseCode: Int): String =
    runCatching {
        JSONObject(this).getJSONObject("error").getString("message")
    }.getOrDefault("러닝 기록 요청에 실패했어요. ($responseCode)")

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
private const val TAG = "RunApi"
