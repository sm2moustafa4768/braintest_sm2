package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.model.GameType
import com.example.ui.components.GameResultDialog
import com.example.ui.games.DualFlankerGame
import com.example.ui.games.MemoryMatrixGame
import com.example.ui.games.MentalMathBlitzGame
import com.example.ui.games.NumberSequenceGame
import com.example.ui.games.SpeedReactionGame
import com.example.ui.games.StroopClashGame
import com.example.ui.screens.ConceptGuideScreen
import com.example.ui.screens.DailyChallengeScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.StatsLeaderboardScreen
import com.example.ui.theme.BrainTrainerTheme
import com.example.ui.viewmodel.BrainTrainerViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    private val viewModel: BrainTrainerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BrainTrainerTheme {
                BrainTrainerApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BrainTrainerApp(viewModel: BrainTrainerViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeGame by viewModel.activeGame.collectAsState()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsState()
    val lastResult by viewModel.lastResult.collectAsState()

    val userProfile by viewModel.userProfile.collectAsState()
    val dailyChallenge by viewModel.todayDailyChallenge.collectAsState()
    val recentSessions by viewModel.recentSessions.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()

    val showBottomNav = currentScreen != Screen.GAME_PLAY

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("main_scaffold"),
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(modifier = Modifier.testTag("bottom_nav_bar")) {
                    NavigationBarItem(
                        selected = currentScreen == Screen.DASHBOARD,
                        onClick = { viewModel.navigateTo(Screen.DASHBOARD) },
                        icon = { Icon(Icons.Default.Psychology, contentDescription = "Games") },
                        label = { Text("Training") },
                        modifier = Modifier.testTag("nav_item_dashboard")
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.DAILY_CHALLENGE,
                        onClick = { viewModel.navigateTo(Screen.DAILY_CHALLENGE) },
                        icon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = "Daily") },
                        label = { Text("Daily") },
                        modifier = Modifier.testTag("nav_item_daily")
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.STATS_LEADERBOARD,
                        onClick = { viewModel.navigateTo(Screen.STATS_LEADERBOARD) },
                        icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Stats") },
                        label = { Text("Stats") },
                        modifier = Modifier.testTag("nav_item_stats")
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.CONCEPT_GUIDE,
                        onClick = { viewModel.navigateTo(Screen.CONCEPT_GUIDE) },
                        icon = { Icon(Icons.Default.Description, contentDescription = "Guide") },
                        label = { Text("Blueprint") },
                        modifier = Modifier.testTag("nav_item_guide")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.DASHBOARD -> {
                    DashboardScreen(
                        userProfile = userProfile,
                        dailyChallenge = dailyChallenge,
                        recentSessions = recentSessions,
                        selectedDifficulty = selectedDifficulty,
                        onDifficultySelected = { viewModel.setDifficulty(it) },
                        onStartGame = { viewModel.startMiniGame(it, isDaily = false) },
                        onOpenDailyChallenge = { viewModel.navigateTo(Screen.DAILY_CHALLENGE) }
                    )
                }

                Screen.DAILY_CHALLENGE -> {
                    DailyChallengeScreen(
                        userProfile = userProfile,
                        dailyChallenge = dailyChallenge,
                        onPlayDailyGame = { viewModel.startMiniGame(it, isDaily = true) },
                        onBackToDashboard = { viewModel.navigateTo(Screen.DASHBOARD) }
                    )
                }

                Screen.STATS_LEADERBOARD -> {
                    StatsLeaderboardScreen(
                        userProfile = userProfile,
                        allSessions = allSessions
                    )
                }

                Screen.CONCEPT_GUIDE -> {
                    ConceptGuideScreen()
                }

                Screen.GAME_PLAY -> {
                    val game = activeGame ?: GameType.MEMORY_MATRIX
                    when (game) {
                        GameType.MEMORY_MATRIX -> {
                            MemoryMatrixGame(
                                difficulty = selectedDifficulty,
                                onGameFinished = { score, acc, ms -> viewModel.finishGame(score, acc, ms) },
                                onExit = { viewModel.navigateTo(Screen.DASHBOARD) }
                            )
                        }
                        GameType.SPEED_REACTION -> {
                            SpeedReactionGame(
                                difficulty = selectedDifficulty,
                                onGameFinished = { score, acc, ms -> viewModel.finishGame(score, acc, ms) },
                                onExit = { viewModel.navigateTo(Screen.DASHBOARD) }
                            )
                        }
                        GameType.STROOP_CLASH -> {
                            StroopClashGame(
                                difficulty = selectedDifficulty,
                                onGameFinished = { score, acc, ms -> viewModel.finishGame(score, acc, ms) },
                                onExit = { viewModel.navigateTo(Screen.DASHBOARD) }
                            )
                        }
                        GameType.MATH_BLITZ -> {
                            MentalMathBlitzGame(
                                difficulty = selectedDifficulty,
                                onGameFinished = { score, acc, ms -> viewModel.finishGame(score, acc, ms) },
                                onExit = { viewModel.navigateTo(Screen.DASHBOARD) }
                            )
                        }
                        GameType.NUMBER_SEQUENCE -> {
                            NumberSequenceGame(
                                difficulty = selectedDifficulty,
                                onGameFinished = { score, acc, ms -> viewModel.finishGame(score, acc, ms) },
                                onExit = { viewModel.navigateTo(Screen.DASHBOARD) }
                            )
                        }
                        GameType.DUAL_FLANKER -> {
                            DualFlankerGame(
                                difficulty = selectedDifficulty,
                                onGameFinished = { score, acc, ms -> viewModel.finishGame(score, acc, ms) },
                                onExit = { viewModel.navigateTo(Screen.DASHBOARD) }
                            )
                        }
                    }
                }
            }

            // Results Dialog when a session completes
            lastResult?.let { result ->
                GameResultDialog(
                    result = result,
                    onPlayAgain = { viewModel.replayGame() },
                    onDone = { viewModel.dismissResult() }
                )
            }
        }
    }
}
