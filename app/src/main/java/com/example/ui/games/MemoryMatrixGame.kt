package com.example.ui.games

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
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

private enum class MatrixState {
    MEMORIZING,
    RECALLING,
    ROUND_SUCCESS,
    ROUND_FAIL
}

@Composable
fun MemoryMatrixGame(
    difficulty: Difficulty,
    onGameFinished: (score: Int, accuracy: Float, avgReactionMs: Long) -> Unit,
    onExit: () -> Unit
) {
    val gridSize = when (difficulty) {
        Difficulty.EASY -> 3
        Difficulty.MEDIUM -> 4
        Difficulty.HARD -> 4
    }
    val totalCells = gridSize * gridSize
    val activeTileCount = when (difficulty) {
        Difficulty.EASY -> 3
        Difficulty.MEDIUM -> 4
        Difficulty.HARD -> 6
    }

    var round by remember { mutableIntStateOf(1) }
    val maxRounds = 5
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    var totalTaps by remember { mutableIntStateOf(0) }
    var correctTaps by remember { mutableIntStateOf(0) }
    var matrixState by remember { mutableStateOf(MatrixState.MEMORIZING) }

    val highlightedTiles = remember { mutableStateListOf<Int>() }
    val selectedTiles = remember { mutableStateListOf<Int>() }
    val wrongTiles = remember { mutableStateListOf<Int>() }

    fun generateNewPattern() {
        highlightedTiles.clear()
        selectedTiles.clear()
        wrongTiles.clear()
        matrixState = MatrixState.MEMORIZING

        val count = (activeTileCount + (round - 1)).coerceAtMost(totalCells - 2)
        val indices = (0 until totalCells).shuffled(Random(System.currentTimeMillis()))
        highlightedTiles.addAll(indices.take(count))
    }

    LaunchedEffect(round) {
        generateNewPattern()
        // Display flash for 1.8 seconds, then switch to recall
        delay(1800)
        matrixState = MatrixState.RECALLING
    }

    fun onTileClick(index: Int) {
        if (matrixState != MatrixState.RECALLING) return
        if (selectedTiles.contains(index) || wrongTiles.contains(index)) return

        totalTaps++
        if (highlightedTiles.contains(index)) {
            selectedTiles.add(index)
            correctTaps++
            score += 100

            // Check if all active tiles were found
            if (selectedTiles.size == highlightedTiles.size) {
                matrixState = MatrixState.ROUND_SUCCESS
                score += 250
            }
        } else {
            wrongTiles.add(index)
            lives--
            if (lives <= 0) {
                val accuracy = if (totalTaps > 0) correctTaps.toFloat() / totalTaps else 0f
                onGameFinished(score, accuracy, 1200L)
            }
        }
    }

    LaunchedEffect(matrixState) {
        if (matrixState == MatrixState.ROUND_SUCCESS) {
            delay(1000)
            if (round >= maxRounds) {
                val accuracy = if (totalTaps > 0) correctTaps.toFloat() / totalTaps else 1f
                onGameFinished(score, accuracy, 950L)
            } else {
                round++
            }
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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ROUND $round / $maxRounds",
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(3) { i ->
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Life ${i + 1}",
                            tint = if (i < lives) Color(0xFFEF4444) else Color(0xFF64748B),
                            modifier = Modifier
                                .size(24.dp)
                                .padding(horizontal = 2.dp)
                        )
                    }
                }
            }
        }

        // Instructions Banner
        val bannerText = when (matrixState) {
            MatrixState.MEMORIZING -> "Memorize the lit tiles..."
            MatrixState.RECALLING -> "Tap the positions you saw!"
            MatrixState.ROUND_SUCCESS -> "Pattern Cleared! +250 XP"
            MatrixState.ROUND_FAIL -> "Oops! Watch carefully."
        }
        val bannerColor = when (matrixState) {
            MatrixState.MEMORIZING -> MaterialTheme.colorScheme.primary
            MatrixState.RECALLING -> MaterialTheme.colorScheme.secondary
            MatrixState.ROUND_SUCCESS -> Color(0xFF10B981)
            MatrixState.ROUND_FAIL -> Color(0xFFEF4444)
        }

        Text(
            text = bannerText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = bannerColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // Matrix Grid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (row in 0 until gridSize) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (col in 0 until gridSize) {
                            val index = row * gridSize + col
                            val isHighlighted = highlightedTiles.contains(index)
                            val isSelected = selectedTiles.contains(index)
                            val isWrong = wrongTiles.contains(index)

                            val tileColor by animateColorAsState(
                                targetValue = when {
                                    matrixState == MatrixState.MEMORIZING && isHighlighted -> Color(0xFF6366F1)
                                    isSelected -> Color(0xFF10B981)
                                    isWrong -> Color(0xFFEF4444)
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                label = "TileColor"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(tileColor)
                                    .clickable(enabled = matrixState == MatrixState.RECALLING) {
                                        onTileClick(index)
                                    }
                                    .testTag("matrix_tile_${row}_${col}")
                            )
                        }
                    }
                }
            }
        }

        // Bottom Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onExit,
                modifier = Modifier.testTag("matrix_quit_button")
            ) {
                Text("End Session")
            }
        }
    }
}
