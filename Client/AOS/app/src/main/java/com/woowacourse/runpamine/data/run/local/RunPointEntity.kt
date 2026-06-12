package com.woowacourse.runpamine.data.run.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.woowacourse.runpamine.domain.run.RunPoint
import java.time.Instant

@Entity(
    tableName = "run_points",
    primaryKeys = ["sessionId", "sequence"],
    foreignKeys = [
        ForeignKey(
            entity = RunSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["sessionId"])],
)
data class RunPointEntity(
    val sessionId: String,
    val sequence: Int,
    val latitude: Double,
    val longitude: Double,
    val recordedAtEpochMillis: Long,
    val horizontalAccuracyMeters: Float?,
)

fun RunPointEntity.toDomain(): RunPoint =
    RunPoint(
        sessionId = sessionId,
        sequence = sequence,
        latitude = latitude,
        longitude = longitude,
        recordedAt = Instant.ofEpochMilli(recordedAtEpochMillis),
        horizontalAccuracyMeters = horizontalAccuracyMeters,
    )

fun RunPoint.toEntity(): RunPointEntity =
    RunPointEntity(
        sessionId = sessionId,
        sequence = sequence,
        latitude = latitude,
        longitude = longitude,
        recordedAtEpochMillis = recordedAt.toEpochMilli(),
        horizontalAccuracyMeters = horizontalAccuracyMeters,
    )
