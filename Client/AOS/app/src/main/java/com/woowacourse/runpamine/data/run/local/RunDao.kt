package com.woowacourse.runpamine.data.run.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.woowacourse.runpamine.domain.run.RunSyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: RunSessionEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPoint(point: RunPointEntity)

    @Query("SELECT * FROM run_sessions WHERE id = :sessionId")
    suspend fun getSession(sessionId: String): RunSessionEntity?

    @Query("SELECT * FROM run_sessions WHERE endedAtEpochMillis IS NULL ORDER BY startedAtEpochMillis DESC LIMIT 1")
    suspend fun getActiveSession(): RunSessionEntity?

    @Query("SELECT * FROM run_sessions WHERE endedAtEpochMillis IS NULL ORDER BY startedAtEpochMillis DESC LIMIT 1")
    fun observeActiveSession(): Flow<RunSessionEntity?>

    @Query("SELECT * FROM run_points WHERE sessionId = :sessionId ORDER BY sequence ASC")
    suspend fun getPoints(sessionId: String): List<RunPointEntity>

    @Query("SELECT * FROM run_points WHERE sessionId = :sessionId ORDER BY sequence ASC")
    fun observePoints(sessionId: String): Flow<List<RunPointEntity>>

    @Query("SELECT * FROM run_points WHERE sessionId = :sessionId ORDER BY sequence DESC LIMIT 1")
    suspend fun getLastPoint(sessionId: String): RunPointEntity?

    @Query("SELECT COUNT(*) FROM run_points WHERE sessionId = :sessionId")
    suspend fun getPointCount(sessionId: String): Int

    @Query(
        """
        UPDATE run_sessions
        SET endedAtEpochMillis = :endedAtEpochMillis,
            distanceMeters = :distanceMeters,
            durationSeconds = :durationSeconds,
            averagePaceSecondsPerKm = :averagePaceSecondsPerKm,
            calories = :calories,
            syncStatus = :syncStatus
        WHERE id = :sessionId
        """,
    )
    suspend fun finishSession(
        sessionId: String,
        endedAtEpochMillis: Long,
        distanceMeters: Int,
        durationSeconds: Long,
        averagePaceSecondsPerKm: Int,
        calories: Int,
        syncStatus: RunSyncStatus = RunSyncStatus.LOCAL_ONLY,
    )

    @Query("DELETE FROM run_sessions WHERE endedAtEpochMillis IS NULL")
    suspend fun deleteActiveSessions()

    @Query(
        """
        UPDATE run_sessions
        SET distanceMeters = :distanceMeters,
            durationSeconds = :durationSeconds,
            averagePaceSecondsPerKm = :averagePaceSecondsPerKm,
            calories = :calories
        WHERE id = :sessionId
        """,
    )
    suspend fun updateRunningMetrics(
        sessionId: String,
        distanceMeters: Int,
        durationSeconds: Long,
        averagePaceSecondsPerKm: Int,
        calories: Int,
    )

    @Query("UPDATE run_sessions SET syncStatus = :syncStatus WHERE id = :sessionId")
    suspend fun updateSyncStatus(
        sessionId: String,
        syncStatus: RunSyncStatus,
    )

    @Query(
        """
        SELECT * FROM run_sessions
        WHERE endedAtEpochMillis IS NOT NULL
          AND syncStatus IN (:statuses)
        ORDER BY startedAtEpochMillis ASC
        """,
    )
    suspend fun getSessionsByStatuses(statuses: List<RunSyncStatus>): List<RunSessionEntity>

    @Transaction
    suspend fun insertPointAndMetrics(
        point: RunPointEntity,
        distanceMeters: Int,
        durationSeconds: Long,
        averagePaceSecondsPerKm: Int,
        calories: Int,
    ) {
        insertPoint(point)
        updateRunningMetrics(
            sessionId = point.sessionId,
            distanceMeters = distanceMeters,
            durationSeconds = durationSeconds,
            averagePaceSecondsPerKm = averagePaceSecondsPerKm,
            calories = calories,
        )
    }
}
