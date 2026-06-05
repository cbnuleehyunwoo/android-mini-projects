package com.woowacourse.runpamine.data.run.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.woowacourse.runpamine.domain.run.RunSession
import com.woowacourse.runpamine.domain.run.RunSyncStatus
import java.time.Instant

@Entity(tableName = "run_sessions")
data class RunSessionEntity(
    @PrimaryKey val id: String,
    val startedAtEpochMillis: Long,
    val endedAtEpochMillis: Long?,
    val distanceMeters: Int,
    val durationSeconds: Long,
    val calories: Int,
    val syncStatus: RunSyncStatus,
)

fun RunSessionEntity.toDomain(): RunSession =
    RunSession(
        id = id,
        startedAt = Instant.ofEpochMilli(startedAtEpochMillis),
        endedAt = endedAtEpochMillis?.let(Instant::ofEpochMilli),
        distanceMeters = distanceMeters,
        durationSeconds = durationSeconds,
        calories = calories,
        syncStatus = syncStatus,
    )

fun RunSession.toEntity(): RunSessionEntity =
    RunSessionEntity(
        id = id,
        startedAtEpochMillis = startedAt.toEpochMilli(),
        endedAtEpochMillis = endedAt?.toEpochMilli(),
        distanceMeters = distanceMeters,
        durationSeconds = durationSeconds,
        calories = calories,
        syncStatus = syncStatus,
    )
