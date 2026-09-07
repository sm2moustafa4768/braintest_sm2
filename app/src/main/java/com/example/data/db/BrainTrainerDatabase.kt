package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.GameDao
import com.example.data.entity.DailyChallengeEntity
import com.example.data.entity.GameSessionRecord
import com.example.data.entity.UserProfileEntity

@Database(
    entities = [GameSessionRecord::class, DailyChallengeEntity::class, UserProfileEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BrainTrainerDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: BrainTrainerDatabase? = null

        fun getDatabase(context: Context): BrainTrainerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BrainTrainerDatabase::class.java,
                    "brain_trainer_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
