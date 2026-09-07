package com.example.ui.games

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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

private data class MathEquation(
    val equationText: String,
    val isCorrect: Boolean
)

@Composable
fun MentalMathBlitzGame(
    difficulty: Difficulty,
    onGameFinished: (score: Int, accuracy: Float, avgReactionMs: Long) -> Unit,
    onExit: () -> Unit
) {
    var secondsRemaining by remember { mutableIntStateOf(25) }
    var score by remember { mutableIntStateOf(0) }
    var totalQuestions by remember { mutableIntStateOf(0) }
    var correctAnswers by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    val responseTimes = remember { mutableStateListOf<Long>() }

    var currentEquation by remember { mutableStateOf(MathEquation("12 + 15 = 27", true)) }
    var questionStartTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    fun generateEquation(): MathEquation {
        val op = when (difficulty) {
            Difficulty.EASY -> listOf("+", "-").random()
            Difficulty.MEDIUM -> listOf("+", "-", "×").random()
            Difficulty.HARD -> listOf("+", "-", "×").random()
        }

        val a: Int
        val b: Int
        val actualResult: Int

        when (op) {
            "+" -> {
                a = Random.nextInt(10, if (difficulty == Difficulty.HARD) 80 else 40)
                b = Random.nextInt(5, if (difficulty == Difficulty.HARD) 50 else 30)
                actualResult = a + b
            }
            "-" -> {
                a = Random.nextInt(20, if (difficulty == Difficulty.HARD) 90 else 50)
                b = Random.nextInt(5, a)
                actualResult = a - b
            }
            else -> { // "×"
                a = Random.nextInt(3, if (difficulty == Difficulty.HARD) 14 else 10)
                b = Random.nextInt(3, if (difficulty == Difficulty.HARD) 12 else 9)
                actualResult = a * b
            }
        }

        val makeCorrect = Random.nextBoolean()
        val displayedResult = if (makeCorrect) {
            actualResult
        } else {
            val offset = listOf(-3, -2, -1, 1, 2, 3, 10).random()
            actualResult + offset
        }

        return MathEquation("$a $op $b = $displayedResult", makeCorrect)
    }

    LaunchedEffect(Unit) {
        currentEquation = generateEquation()
        questionStartTime = System.currentTimeMillis()
    }

    // Blitz countdown clock
    LaunchedEffect(secondsRemaining) {
        if (secondsRemaining > 0) {
            delay(1000)
            secondsRemaining--
        } else {
            val accuracy = if (totalQuestions > 0) correctAnswers.toFloat() / totalQuestions else 0f
            val avgMs = if (responseTimes.isNotEmpty()) responseTimes.average().toLong() else 1800L
            onGameFinished(score, accuracy, avgMs)
        }
    }

    fun submitAnswer(userSaysCorrect: Boolean) {
        val delta = (System.currentTimeMillis() - questionStartTime).coerceAtLeast(150)
        responseTimes.add(delta)
        totalQuestions++

        if (userSaysCorrect == currentEquation.isCorrect) {
            correctAnswers++
            streak++
            val streakBonus = (streak * 15).coerceAtMost(100)
            score += 100 + streakBonus
            secondsRemaining = (secondsRemaining + 1).coerceAtMost(40) // time bonus
        } else {
            streak = 0
            secondsRemaining = (secondsRemaining - 2).coerceAtLeast(0)
        }

        currentEquation = generateEquation()
        questionStartTime = System.currentTimeMillis()
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
                    Column {
                        Text(
                            text = "TIME LEFT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${secondsRemaining}s",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = if (secondsRemaining <= 5) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "STREAK: $streak",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (streak >= 3) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Score: $score",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val progress = (secondsRemaining / 30f).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (secondsRemaining <= 5) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                )
            }
        }

        // Equation display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "IS THIS EQUATION ACCURATE?",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = currentEquation.equationText,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Action Buttons: False vs True
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { submitAnswer(false) },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .testTag("math_false_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "False", tint = Color.White)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("FALSE", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                }
            }

            Button(
                onClick = { submitAnswer(true) },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .testTag("math_true_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "True", tint = Color.White)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("TRUE", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                }
            }
        }

        // End Game button
        Button(
            onClick = onExit,
            modifier = Modifier.testTag("math_quit_btn")
        ) {
            Text("End Blitz")
        }
    }
}
