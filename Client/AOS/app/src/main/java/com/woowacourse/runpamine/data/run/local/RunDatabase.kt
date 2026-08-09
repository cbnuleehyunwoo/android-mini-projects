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
    version = 5,
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

        val MIGRATION_3_4 =
            object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        ALTER TABLE run_sessions
                        ADD COLUMN accountUserId TEXT
                        """.trimIndent(),
                    )
                    // 기존 스키마의 정밀 위치에는 계정 소유자 정보가 없어 안전하게 재전송할 수 없다.
                    db.execSQL("DELETE FROM run_points")
                    db.execSQL("DELETE FROM run_sessions")
                }
            }

        val MIGRATION_4_5 =
            object : Migration(4, 5) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        ALTER TABLE run_sessions
                        ADD COLUMN splitsJson TEXT NOT NULL DEFAULT '[]'
                        """.trimIndent(),
                    )
                }
            }
    }
}
