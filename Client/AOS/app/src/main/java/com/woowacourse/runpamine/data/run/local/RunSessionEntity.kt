package com.woowacourse.runpamine.data.run.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.woowacourse.runpamine.domain.run.RunSession
import com.woowacourse.runpamine.domain.run.RunSplit
import com.woowacourse.runpamine.domain.run.RunSyncStatus
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

@Entity(tableName = "run_sessions")
data class RunSessionEntity(
    @PrimaryKey val id: String,
    val startedAtEpochMillis: Long,
    val endedAtEpochMillis: Long?,
    val distanceMeters: Int,
    val durationSeconds: Long,
    val averagePaceSecondsPerKm: Int,
    val calories: Int,
    val syncStatus: RunSyncStatus,
    val accountUserId: String?,
    val splitsJson: String,
)

fun RunSessionEntity.toDomain(): RunSession =
    RunSession(
        id = id,
        startedAt = Instant.ofEpochMilli(startedAtEpochMillis),
        endedAt = endedAtEpochMillis?.let(Instant::ofEpochMilli),
        distanceMeters = distanceMeters,
        durationSeconds = durationSeconds,
        averagePaceSecondsPerKm = averagePaceSecondsPerKm,
        calories = calories,
        syncStatus = syncStatus,
        accountUserId = accountUserId,
        splits = splitsJson.toRunSplits(),
    )

fun RunSession.toEntity(): RunSessionEntity =
    RunSessionEntity(
        id = id,
        startedAtEpochMillis = startedAt.toEpochMilli(),
        endedAtEpochMillis = endedAt?.toEpochMilli(),
        distanceMeters = distanceMeters,
        durationSeconds = durationSeconds,
        averagePaceSecondsPerKm = averagePaceSecondsPerKm,
        calories = calories,
        syncStatus = syncStatus,
        accountUserId = accountUserId,
        splitsJson = splits.toJsonString(),
    )

internal fun List<RunSplit>.toJsonString(): String =
    JSONArray(
        map { split ->
            JSONObject()
                .put("sequence", split.sequence)
                .put("fromDistanceMeters", split.fromDistanceMeters)
                .put("toDistanceMeters", split.toDistanceMeters)
                .put("distanceMeters", split.distanceMeters)
                .put("durationMillis", split.durationMillis)
                .put("paceSecondsPerKm", split.paceSecondsPerKm)
        },
    ).toString()

private fun String.toRunSplits(): List<RunSplit> =
    runCatching {
        val array = JSONArray(this)
        List(array.length()) { index ->
            array.getJSONObject(index).let { split ->
                RunSplit(
                    sequence = split.getInt("sequence"),
                    fromDistanceMeters = split.getInt("fromDistanceMeters"),
                    toDistanceMeters = split.getInt("toDistanceMeters"),
                    distanceMeters = split.getInt("distanceMeters"),
                    durationMillis = split.getLong("durationMillis"),
                    paceSecondsPerKm = split.getDouble("paceSecondsPerKm"),
                )
            }
        }
    }.getOrDefault(emptyList())
