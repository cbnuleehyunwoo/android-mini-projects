package com.woowacourse.runpamine.data.run.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        RunSessionEntity::class,
        RunPointEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class RunDatabase : RoomDatabase() {
    abstract fun runDao(): RunDao

    companion object {
        val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        ALTER TABLE run_sessions
                        ADD COLUMN averagePaceSecondsPerKm INTEGER NOT NULL DEFAULT 0
                        """.trimIndent(),
                    )
                }
            }

        val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        ALTER TABLE run_points
                        ADD COLUMN horizontalAccuracyMeters REAL
                        """.trimIndent(),
                    )
                }
            }
    }
}
