package com.example.ui.games

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import kotlin.random.Random

private data class SequencePuzzle(
    val numbers: List<String>,
    val correctAnswer: Int,
    val options: List<Int>,
    val explanation: String
)

@Composable
fun NumberSequenceGame(
    difficulty: Difficulty,
    onGameFinished: (score: Int, accuracy: Float, avgReactionMs: Long) -> Unit,
    onExit: () -> Unit
) {
    val totalRounds = 6
    var currentRound by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    val responseTimes = remember { mutableStateListOf<Long>() }
    var questionStartTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var currentPuzzle by remember {
        mutableStateOf(
            SequencePuzzle(
                numbers = listOf("2", "4", "6", "8", "?"),
                correctAnswer = 10,
                options = listOf(9, 10, 12, 14),
                explanation = "+2 each step"
            )
        )
    }

    fun generatePuzzle(): SequencePuzzle {
        val type = Random.nextInt(0, if (difficulty == Difficulty.EASY) 2 else 4)
        return when (type) {
            0 -> {
                // Arithmetic sequence
                val step = Random.nextInt(3, 9)
                val start = Random.nextInt(2, 20)
                val n1 = start
                val n2 = n1 + step
                val n3 = n2 + step
                val n4 = n3 + step
                val ans = n4 + step
                val opts = listOf(ans, ans - step, ans + step, ans + 1).shuffled()
                SequencePuzzle(listOf("$n1", "$n2", "$n3", "$n4", "?"), ans, opts, "+$step sequence")
            }
            1 -> {
                // Subtraction sequence
                val step = Random.nextInt(3, 8)
                val start = Random.nextInt(50, 90)
                val n1 = start
                val n2 = n1 - step
                val n3 = n2 - step
                val n4 = n3 - step
                val ans = n4 - step
                val opts = listOf(ans, ans + step, ans - step, ans - 1).shuffled()
                SequencePuzzle(listOf("$n1", "$n2", "$n3", "$n4", "?"), ans, opts, "-$step sequence")
            }
            2 -> {
                // Geometric sequence
                val mult = listOf(2, 3).random()
                val start = Random.nextInt(2, 5)
                val n1 = start
                val n2 = n1 * mult
                val n3 = n2 * mult
                val ans = n3 * mult
                val opts = listOf(ans, ans - mult, ans + 4, ans * 2).shuffled()
                SequencePuzzle(listOf("$n1", "$n2", "$n3", "?"), ans, opts, "x$mult progression")
            }
            else -> {
                // Alternating +/-
                val base = Random.nextInt(5, 15)
                val n1 = base
                val n2 = n1 + 5
                val n3 = n2 - 2
                val n4 = n3 + 5
                val n5 = n4 - 2
                val ans = n5 + 5
                val opts = listOf(ans, ans - 2, ans + 3, ans - 5).shuffled()
                SequencePuzzle(listOf("$n1", "$n2", "$n3", "$n4", "$n5", "?"), ans, opts, "+5, -2 pattern")
            }
        }
    }

    LaunchedEffect(currentRound) {
        currentPuzzle = generatePuzzle()
        questionStartTime = System.currentTimeMillis()
    }

    fun onOptionSelected(chosen: Int) {
        val delta = (System.currentTimeMillis() - questionStartTime).coerceAtLeast(200)
        responseTimes.add(delta)

        if (chosen == currentPuzzle.correctAnswer) {
            correctCount++
            val timeBonus = ((4000 - delta) / 20).coerceAtLeast(0)
            score += 150 + timeBonus.toInt()
        }

        if (currentRound >= totalRounds) {
            val accuracy = correctCount.toFloat() / totalRounds
            val avg = if (responseTimes.isNotEmpty()) responseTimes.average().toLong() else 1800L
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PUZZLE $currentRound / $totalRounds",
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

        // Sequence Display Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "IDENTIFY THE MISSING NUMBER",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    currentPuzzle.numbers.forEach { item ->
                        val isQuestion = item == "?"
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isQuestion) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = if (isQuestion) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 4 Options (2x2 Grid)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val options = currentPuzzle.options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (opt in options.take(2)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { onOptionSelected(opt) }
                            .testTag("seq_opt_$opt"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$opt",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (opt in options.drop(2)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { onOptionSelected(opt) }
                            .testTag("seq_opt_$opt"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$opt",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Exit
        Button(
            onClick = onExit,
            modifier = Modifier.testTag("sequence_exit_btn")
        ) {
            Text("End Game")
        }
    }
}
