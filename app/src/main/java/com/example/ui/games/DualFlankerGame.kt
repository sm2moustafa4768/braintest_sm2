package com.example.ui.games

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Difficulty
import kotlin.random.Random

private enum class ArrowDirection { LEFT, RIGHT }

private data class FlankerTrial(
    val flankers: ArrowDirection,
    val center: ArrowDirection
)

@Composable
fun DualFlankerGame(
    difficulty: Difficulty,
    onGameFinished: (score: Int, accuracy: Float, avgReactionMs: Long) -> Unit,
    onExit: () -> Unit
) {
    val totalTrials = 10
    var currentTrial by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    val reactionTimes = remember { mutableStateListOf<Long>() }
    var trialStartTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var trial by remember {
        mutableStateOf(FlankerTrial(ArrowDirection.RIGHT, ArrowDirection.RIGHT))
    }

    fun nextTrial() {
        val center = if (Random.nextBoolean()) ArrowDirection.LEFT else ArrowDirection.RIGHT
        // 60% chance incongruent on medium/hard
        val isIncongruent = Random.nextFloat() < (if (difficulty == Difficulty.EASY) 0.35f else 0.65f)
        val flankers = if (isIncongruent) {
            if (center == ArrowDirection.LEFT) ArrowDirection.RIGHT else ArrowDirection.LEFT
        } else {
            center
        }
        trial = FlankerTrial(flankers, center)
        trialStartTime = System.currentTimeMillis()
    }

    LaunchedEffect(currentTrial) {
        nextTrial()
    }

    fun handleUserChoice(choice: ArrowDirection) {
        val delta = (System.currentTimeMillis() - trialStartTime).coerceAtLeast(150)
        reactionTimes.add(delta)

        if (choice == trial.center) {
            correctCount++
            val timeBonus = ((1500 - delta) / 10).coerceAtLeast(0)
            score += 100 + timeBonus.toInt()
        }

        if (currentTrial >= totalTrials) {
            val accuracy = correctCount.toFloat() / totalTrials
            val avg = if (reactionTimes.isNotEmpty()) reactionTimes.average().toLong() else 500L
            onGameFinished(score, accuracy, avg)
        } else {
            currentTrial++
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRIAL $currentTrial / $totalTrials",
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
        }

        // Flanker Array Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "FOCUS ONLY ON THE CENTER ARROW",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(28.dp))

                // 5 arrows: 2 flankers, 1 center, 2 flankers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val arrowSymbol = { dir: ArrowDirection -> if (dir == ArrowDirection.LEFT) "◀" else "▶" }

                    // Flanker 1 & 2
                    Text(
                        text = "${arrowSymbol(trial.flankers)}  ${arrowSymbol(trial.flankers)}  ",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )

                    // CENTER TARGET ARROW (Highlighted)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = arrowSymbol(trial.center),
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Flanker 4 & 5
                    Text(
                        text = "  ${arrowSymbol(trial.flankers)}  ${arrowSymbol(trial.flankers)}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Two Large Directional Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { handleUserChoice(ArrowDirection.LEFT) },
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp)
                    .testTag("flanker_btn_left"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Left",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("LEFT", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                }
            }

            Button(
                onClick = { handleUserChoice(ArrowDirection.RIGHT) },
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp)
                    .testTag("flanker_btn_right"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("RIGHT", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                    Spacer(modifier = Modifier.size(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Right",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Exit
        Button(
            onClick = onExit,
            modifier = Modifier.testTag("flanker_exit_btn")
        ) {
            Text("End Game")
        }
    }
}
