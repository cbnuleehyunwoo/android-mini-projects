package com.woowacourse.runpamine.data.run.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        RunSessionEntity::class,
        RunPointEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class RunDatabase : RoomDatabase() {
    abstract fun runDao(): RunDao
}
