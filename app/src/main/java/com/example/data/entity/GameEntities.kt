package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_sessions")
data class GameSessionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameId: String,
    val category: String,
    val score: Int,
    val reactionTimeMs: Long,
    val accuracy: Float,
    val difficulty: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isDailyChallenge: Boolean = false
)

@Entity(tableName = "daily_challenges")
data class DailyChallengeEntity(
    @PrimaryKey
    val dateKey: String, // Format "yyyy-MM-dd"
    val completedGameIds: String = "", // Comma-separated game ids
    val isCompleted: Boolean = false,
    val xpEarned: Int = 0
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val username: String = "Cognitive Explorer",
    val totalXp: Int = 150,
    val streakDays: Int = 1,
    val lastActiveDate: String = "",
    val brainScore: Int = 650
)
