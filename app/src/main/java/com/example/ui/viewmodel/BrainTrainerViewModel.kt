package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.BrainTrainerDatabase
import com.example.data.entity.DailyChallengeEntity
import com.example.data.entity.GameSessionRecord
import com.example.data.entity.UserProfileEntity
import com.example.data.repository.BrainTrainerRepository
import com.example.model.Difficulty
import com.example.model.GameType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    DASHBOARD,
    GAME_PLAY,
    STATS_LEADERBOARD,
    DAILY_CHALLENGE,
    CONCEPT_GUIDE
}

data class LastGameResult(
    val gameType: GameType,
    val score: Int,
    val accuracy: Float,
    val reactionTimeMs: Long,
    val xpEarned: Int,
    val isNewHighScore: Boolean = false
)

class BrainTrainerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BrainTrainerRepository

    init {
        val database = BrainTrainerDatabase.getDatabase(application)
        repository = BrainTrainerRepository(database.gameDao())
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    private val _currentScreen = MutableStateFlow(Screen.DASHBOARD)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _activeGame = MutableStateFlow<GameType?>(null)
    val activeGame: StateFlow<GameType?> = _activeGame.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow(Difficulty.MEDIUM)
    val selectedDifficulty: StateFlow<Difficulty> = _selectedDifficulty.asStateFlow()

    private val _isDailyMode = MutableStateFlow(false)
    val isDailyMode: StateFlow<Boolean> = _isDailyMode.asStateFlow()

    private val _lastResult = MutableStateFlow<LastGameResult?>(null)
    val lastResult: StateFlow<LastGameResult?> = _lastResult.asStateFlow()

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentSessions: StateFlow<List<GameSessionRecord>> = repository.recentSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessions: StateFlow<List<GameSessionRecord>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayDailyChallenge: StateFlow<DailyChallengeEntity?> = repository.getTodayDailyChallenge()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setDifficulty(difficulty: Difficulty) {
        _selectedDifficulty.value = difficulty
    }

    fun startMiniGame(gameType: GameType, isDaily: Boolean = false) {
        _activeGame.value = gameType
        _isDailyMode.value = isDaily
        _lastResult.value = null
        _currentScreen.value = Screen.GAME_PLAY
    }

    fun finishGame(score: Int, accuracy: Float, avgReactionMs: Long) {
        val game = _activeGame.value ?: return
        val difficulty = _selectedDifficulty.value
        val finalScore = (score * difficulty.multiplier).toInt()
        val xp = (finalScore / 10).coerceAtLeast(15) + (accuracy * 20).toInt()

        viewModelScope.launch {
            repository.recordGameSession(
                gameId = game.id,
                score = finalScore,
                accuracy = accuracy,
                reactionTimeMs = avgReactionMs,
                difficulty = difficulty.name,
                isDaily = _isDailyMode.value
            )
            _lastResult.value = LastGameResult(
                gameType = game,
                score = finalScore,
                accuracy = accuracy,
                reactionTimeMs = avgReactionMs,
                xpEarned = xp
            )
        }
    }

    fun dismissResult() {
        _lastResult.value = null
        if (_isDailyMode.value) {
            _currentScreen.value = Screen.DAILY_CHALLENGE
        } else {
            _currentScreen.value = Screen.DASHBOARD
        }
    }

    fun replayGame() {
        _lastResult.value = null
        // Remains on Screen.GAME_PLAY with clean state
    }
}
