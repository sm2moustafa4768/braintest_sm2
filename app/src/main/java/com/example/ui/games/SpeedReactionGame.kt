package com.example.ui.games

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Difficulty
import kotlinx.coroutines.delay
import kotlin.random.Random

private enum class ReactionState {
    READY,
    WAITING,
    TRIGGERED,
    TOO_EARLY,
    RECORDED
}

@Composable
fun SpeedReactionGame(
    difficulty: Difficulty,
    onGameFinished: (score: Int, accuracy: Float, avgReactionMs: Long) -> Unit,
    onExit: () -> Unit
) {
    val totalRounds = 5
    var currentRound by remember { mutableIntStateOf(1) }
    var reactionState by remember { mutableStateOf(ReactionState.READY) }
    var triggerTimestamp by remember { mutableLongStateOf(0L) }
    var lastReactionMs by remember { mutableLongStateOf(0L) }
    val roundTimes = remember { mutableStateListOf<Long>() }

    fun startWaitCycle() {
        reactionState = ReactionState.WAITING
    }

    LaunchedEffect(reactionState, currentRound) {
        if (reactionState == ReactionState.WAITING) {
            val waitTime = Random.nextLong(1500, 3800)
            delay(waitTime)
            if (reactionState == ReactionState.WAITING) {
                triggerTimestamp = System.currentTimeMillis()
                reactionState = ReactionState.TRIGGERED
            }
        }
    }

    fun handleSurfaceTap() {
        when (reactionState) {
            ReactionState.READY -> {
                startWaitCycle()
            }
            ReactionState.WAITING -> {
                // False start
                reactionState = ReactionState.TOO_EARLY
                lastReactionMs = 600L
                roundTimes.add(600L)
            }
            ReactionState.TRIGGERED -> {
                val now = System.currentTimeMillis()
                val delta = (now - triggerTimestamp).coerceAtLeast(120)
                lastReactionMs = delta
                roundTimes.add(delta)
                reactionState = ReactionState.RECORDED
            }
            ReactionState.TOO_EARLY, ReactionState.RECORDED -> {
                if (currentRound >= totalRounds) {
                    val validTimes = roundTimes.filter { it < 600L }
                    val avgTime = if (roundTimes.isNotEmpty()) roundTimes.average().toLong() else 400L
                    val accuracy = if (roundTimes.isNotEmpty()) (validTimes.size.toFloat() / roundTimes.size) else 0.8f
                    val score = ((650 - avgTime).coerceAtLeast(50) * 2.5f).toInt()
                    onGameFinished(score, accuracy, avgTime)
                } else {
                    currentRound++
                    startWaitCycle()
                }
            }
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = when (reactionState) {
            ReactionState.READY -> MaterialTheme.colorScheme.surfaceVariant
            ReactionState.WAITING -> Color(0xFFD97706) // Amber
            ReactionState.TRIGGERED -> Color(0xFF10B981) // Neon Green
            ReactionState.TOO_EARLY -> Color(0xFFEF4444) // Red
            ReactionState.RECORDED -> Color(0xFF4F46E5) // Electric Indigo
        },
        label = "BgColor"
    )

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
                Column {
                    Text(
                        text = "ROUND $currentRound / $totalRounds",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (roundTimes.isNotEmpty()) "Avg: ${roundTimes.average().toLong()} ms" else "Reflex Test",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                val fastest = roundTimes.minOrNull()
                if (fastest != null) {
                    Text(
                        text = "Fastest: $fastest ms",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }

        // Main Tap Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(backgroundColor)
                .clickable { handleSurfaceTap() }
                .testTag("reaction_tap_area"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                val iconVector = Icons.Default.Bolt
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = "Lightning",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                val headerMessage = when (reactionState) {
                    ReactionState.READY -> "TAP TO BEGIN"
                    ReactionState.WAITING -> "WAIT FOR GREEN..."
                    ReactionState.TRIGGERED -> "TAP NOW!"
                    ReactionState.TOO_EARLY -> "TOO EARLY!"
                    ReactionState.RECORDED -> "$lastReactionMs ms"
                }

                val subMessage = when (reactionState) {
                    ReactionState.READY -> "Hold finger ready to tap once screen turns green"
                    ReactionState.WAITING -> "Stay calm, do not tap until it turns green"
                    ReactionState.TRIGGERED -> "Strike as fast as possible!"
                    ReactionState.TOO_EARLY -> "False trigger penalty applied. Tap to retry."
                    ReactionState.RECORDED -> if (currentRound >= totalRounds) "Tap to finalize score" else "Tap anywhere for next round"
                }

                Text(
                    text = headerMessage,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = subMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Footer Action
        Button(
            onClick = onExit,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .testTag("reaction_exit_button")
        ) {
            Text("End Game")
        }
    }
}
