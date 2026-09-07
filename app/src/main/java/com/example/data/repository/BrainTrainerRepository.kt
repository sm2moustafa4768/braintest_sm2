package com.example.data.repository

import com.example.data.dao.GameDao
import com.example.data.entity.DailyChallengeEntity
import com.example.data.entity.GameSessionRecord
import com.example.data.entity.UserProfileEntity
import com.example.model.CognitiveCategory
import com.example.model.GameType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BrainTrainerRepository(private val dao: GameDao) {

    val recentSessions: Flow<List<GameSessionRecord>> = dao.getRecentSessions(20)
    val allSessions: Flow<List<GameSessionRecord>> = dao.getAllSessions()
    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()

    fun getTodayKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun getTodayDailyChallenge(): Flow<DailyChallengeEntity?> {
        return dao.getDailyChallenge(getTodayKey())
    }

    fun getLeaderboardForGame(gameId: String): Flow<List<GameSessionRecord>> {
        return dao.getLeaderboardForGame(gameId)
    }

    fun getBestScore(gameId: String): Flow<Int?> {
        return dao.getBestScoreForGame(gameId)
    }

    suspend fun recordGameSession(
        gameId: String,
        score: Int,
        accuracy: Float,
        reactionTimeMs: Long,
        difficulty: String,
        isDaily: Boolean
    ): GameSessionRecord {
        val game = GameType.fromId(gameId)
        val session = GameSessionRecord(
            gameId = gameId,
            category = game.category.name,
            score = score,
            reactionTimeMs = reactionTimeMs,
            accuracy = accuracy,
            difficulty = difficulty,
            timestamp = System.currentTimeMillis(),
            isDailyChallenge = isDaily
        )
        dao.insertSession(session)

        // Update User Profile & XP
        val currentProfile = dao.getUserProfile().firstOrNull() ?: UserProfileEntity()
        val earnedXp = (score / 10).coerceAtLeast(15) + (accuracy * 20).toInt()
        val todayStr = getTodayKey()
        
        var newStreak = currentProfile.streakDays
        if (currentProfile.lastActiveDate.isNotEmpty() && currentProfile.lastActiveDate != todayStr) {
            newStreak += 1
        } else if (currentProfile.lastActiveDate.isEmpty()) {
            newStreak = 1
        }

        val newBrainScore = calculateBrainScoreIndex(currentProfile.brainScore, score, accuracy)

        val updatedProfile = currentProfile.copy(
            totalXp = currentProfile.totalXp + earnedXp,
            streakDays = newStreak,
            lastActiveDate = todayStr,
            brainScore = newBrainScore
        )
        dao.saveUserProfile(updatedProfile)

        // Update Daily Challenge if needed
        if (isDaily) {
            val daily = dao.getDailyChallenge(todayStr).firstOrNull() ?: DailyChallengeEntity(dateKey = todayStr)
            val currentCompleted = daily.completedGameIds.split(",").filter { it.isNotBlank() }.toMutableSet()
            currentCompleted.add(gameId)
            val completedStr = currentCompleted.joinToString(",")
            val isNowCompleted = currentCompleted.size >= 3
            val bonusXp = if (isNowCompleted && !daily.isCompleted) 150 else daily.xpEarned
            dao.saveDailyChallenge(
                daily.copy(
                    completedGameIds = completedStr,
                    isCompleted = isNowCompleted,
                    xpEarned = bonusXp
                )
            )
        }

        return session
    }

    private fun calculateBrainScoreIndex(currentScore: Int, recentGameScore: Int, accuracy: Float): Int {
        val performance = (recentGameScore * 0.4f + accuracy * 600f).toInt()
        val updated = (currentScore * 0.85f + performance * 0.15f).toInt()
        return updated.coerceIn(200, 1000)
    }

    suspend fun seedInitialDataIfEmpty() {
        val sessions = dao.getRecentSessions(1).firstOrNull()
        if (sessions.isNullOrEmpty()) {
            // Seed a few initial demo scores to give the leaderboards realistic context
            val now = System.currentTimeMillis()
            val seedSessions = listOf(
                GameSessionRecord(gameId = "MEMORY_MATRIX", category = "MEMORY", score = 620, reactionTimeMs = 1200, accuracy = 0.90f, difficulty = "MEDIUM", timestamp = now - 86400000),
                GameSessionRecord(gameId = "SPEED_REACTION", category = "SPEED", score = 850, reactionTimeMs = 238, accuracy = 1.0f, difficulty = "HARD", timestamp = now - 50000000),
                GameSessionRecord(gameId = "STROOP_CLASH", category = "FOCUS", score = 740, reactionTimeMs = 450, accuracy = 0.92f, difficulty = "MEDIUM", timestamp = now - 35000000),
                GameSessionRecord(gameId = "MATH_BLITZ", category = "LOGIC", score = 910, reactionTimeMs = 820, accuracy = 0.96f, difficulty = "HARD", timestamp = now - 20000000),
                GameSessionRecord(gameId = "NUMBER_SEQUENCE", category = "LOGIC", score = 680, reactionTimeMs = 1500, accuracy = 0.88f, difficulty = "EASY", timestamp = now - 12000000),
                GameSessionRecord(gameId = "DUAL_FLANKER", category = "FOCUS", score = 810, reactionTimeMs = 310, accuracy = 0.95f, difficulty = "MEDIUM", timestamp = now - 5000000)
            )
            seedSessions.forEach { dao.insertSession(it) }
            dao.saveUserProfile(
                UserProfileEntity(
                    id = 1,
                    username = "Neuro Trainer",
                    totalXp = 480,
                    streakDays = 4,
                    lastActiveDate = getTodayKey(),
                    brainScore = 745
                )
            )
        }
    }
}
