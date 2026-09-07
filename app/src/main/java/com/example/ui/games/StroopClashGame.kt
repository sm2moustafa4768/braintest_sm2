package com.example.ui.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Difficulty
import kotlinx.coroutines.delay
import kotlin.random.Random

private data class ColorItem(val name: String, val color: Color)

private val COLOR_PALETTE = listOf(
    ColorItem("RED", Color(0xFFEF4444)),
    ColorItem("BLUE", Color(0xFF3B82F6)),
    ColorItem("GREEN", Color(0xFF10B981)),
    ColorItem("YELLOW", Color(0xFFF59E0B))
)

@Composable
fun StroopClashGame(
    difficulty: Difficulty,
    onGameFinished: (score: Int, accuracy: Float, avgReactionMs: Long) -> Unit,
    onExit: () -> Unit
) {
    val totalRounds = 8
    var currentRound by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var correctAnswers by remember { mutableIntStateOf(0) }
    val reactionTimes = remember { mutableStateListOf<Long>() }

    var displayedWord by remember { mutableStateOf(COLOR_PALETTE[0].name) }
    var fontColor by remember { mutableStateOf(COLOR_PALETTE[1].color) }
    var targetColorName by remember { mutableStateOf(COLOR_PALETTE[1].name) }
    var promptQuestion by remember { mutableStateOf("TAP THE FONT COLOR") }
    var roundStartTime by remember { mutableLongStateOf(0L) }

    val timerDurationMs = when (difficulty) {
        Difficulty.EASY -> 3000L
        Difficulty.MEDIUM -> 2400L
        Difficulty.HARD -> 1800L
    }
    var timeRemainingMs by remember { mutableLongStateOf(timerDurationMs) }

    fun nextQuestion() {
        val wordItem = COLOR_PALETTE.random()
        var colorItem = COLOR_PALETTE.random()
        // High likelihood of mismatch to trigger Stroop interference
        if (Random.nextFloat() < 0.75f && colorItem.name == wordItem.name) {
            colorItem = COLOR_PALETTE.filter { it.name != wordItem.name }.random()
        }

        val askForFontColor = Random.nextBoolean()
        displayedWord = wordItem.name
        fontColor = colorItem.color
        promptQuestion = if (askForFontColor) "MATCH THE FONT COLOR" else "MATCH THE WRITTEN WORD"
        targetColorName = if (askForFontColor) colorItem.name else wordItem.name
        roundStartTime = System.currentTimeMillis()
        timeRemainingMs = timerDurationMs
    }

    LaunchedEffect(currentRound) {
        nextQuestion()
    }

    // Countdown loop
    LaunchedEffect(currentRound, timeRemainingMs) {
        if (timeRemainingMs > 0) {
            delay(100)
            timeRemainingMs -= 100
        } else {
            // Time ran out for this round
            reactionTimes.add(timerDurationMs)
            if (currentRound >= totalRounds) {
                val accuracy = correctAnswers.toFloat() / totalRounds
                val avg = if (reactionTimes.isNotEmpty()) reactionTimes.average().toLong() else 2000L
                onGameFinished(score, accuracy, avg)
            } else {
                currentRound++
            }
        }
    }

    fun handleAnswer(selectedColorName: String) {
        val delta = (System.currentTimeMillis() - roundStartTime).coerceAtLeast(150)
        reactionTimes.add(delta)

        if (selectedColorName == targetColorName) {
            correctAnswers++
            val timeBonus = ((timerDurationMs - delta) / 10).coerceAtLeast(0)
            score += (100 + timeBonus).toInt()
        }

        if (currentRound >= totalRounds) {
            val accuracy = correctAnswers.toFloat() / totalRounds
            val avg = if (reactionTimes.isNotEmpty()) reactionTimes.average().toLong() else 1500L
            onGameFinished(score, accuracy, avg)
        } else {
            currentRound++
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top HUD
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "QUESTION $currentRound / $totalRounds",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Score: $score",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val progress = (timeRemainingMs.toFloat() / timerDurationMs).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (progress > 0.3f) MaterialTheme.colorScheme.primary else Color(0xFFEF4444)
                )
            }
        }

        // Center Word & Question Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = promptQuestion,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = displayedWord,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = fontColor,
                    textAlign = TextAlign.Center
                )
            }
        }

        // 4 Color Response Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (item in COLOR_PALETTE.take(2)) {
                    Button(
                        onClick = { handleAnswer(item.name) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("stroop_btn_${item.name}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = item.color)
                    ) {
                        Text(
                            text = item.name,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (item in COLOR_PALETTE.drop(2)) {
                    Button(
                        onClick = { handleAnswer(item.name) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("stroop_btn_${item.name}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = item.color)
                    ) {
                        Text(
                            text = item.name,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Exit button
        Button(
            onClick = onExit,
            modifier = Modifier.testTag("stroop_exit_btn")
        ) {
            Text("End Game")
        }
    }
}
