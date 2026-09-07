package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.DailyChallengeEntity
import com.example.data.entity.GameSessionRecord
import com.example.data.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: GameSessionRecord): Long

    @Query("SELECT * FROM game_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<GameSessionRecord>>

    @Query("SELECT * FROM game_sessions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 15): Flow<List<GameSessionRecord>>

    @Query("SELECT MAX(score) FROM game_sessions WHERE gameId = :gameId")
    fun getBestScoreForGame(gameId: String): Flow<Int?>

    @Query("SELECT * FROM game_sessions WHERE gameId = :gameId ORDER BY score DESC LIMIT 10")
    fun getLeaderboardForGame(gameId: String): Flow<List<GameSessionRecord>>

    @Query("SELECT * FROM daily_challenges WHERE dateKey = :dateKey LIMIT 1")
    fun getDailyChallenge(dateKey: String): Flow<DailyChallengeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDailyChallenge(dailyChallenge: DailyChallengeEntity)

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfileEntity)

    @Query("DELETE FROM game_sessions")
    suspend fun clearHistory()
}
