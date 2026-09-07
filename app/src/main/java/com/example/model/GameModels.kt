package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.FocusTeal
import com.example.ui.theme.LogicAmber
import com.example.ui.theme.MemoryPurple
import com.example.ui.theme.SpeedEmerald

enum class CognitiveCategory(val title: String, val color: Color) {
    MEMORY("Memory", MemoryPurple),
    FOCUS("Focus", FocusTeal),
    SPEED("Reaction Speed", SpeedEmerald),
    LOGIC("Logical Thinking", LogicAmber)
}

enum class Difficulty(val label: String, val multiplier: Float) {
    EASY("Easy", 1.0f),
    MEDIUM("Medium", 1.5f),
    HARD("Hard", 2.0f)
}

enum class GameType(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: CognitiveCategory,
    val description: String,
    val rules: List<String>
) {
    MEMORY_MATRIX(
        id = "MEMORY_MATRIX",
        title = "Memory Matrix",
        subtitle = "Spatial working memory",
        category = CognitiveCategory.MEMORY,
        description = "Memorize flashing tile patterns on a grid, then tap all activated positions from memory.",
        rules = listOf(
            "Observe the grid carefully for 1.5 seconds while tiles highlight.",
            "Once tiles hide, tap the exact positions that were lit up.",
            "Complete rounds accurately to advance to larger grids and earn bonus points.",
            "Making 3 errors ends the session."
        )
    ),
    SPEED_REACTION(
        id = "SPEED_REACTION",
        title = "Reaction Tap",
        subtitle = "Reflex & motor speed",
        category = CognitiveCategory.SPEED,
        description = "Wait for the visual cue and tap the screen as rapidly as humanly possible.",
        rules = listOf(
            "Keep your finger poised over the screen during the waiting state (Amber).",
            "When the target bursts into vibrant GREEN, tap immediately!",
            "Avoid false starts — tapping early triggers a penalty.",
            "Play 5 rounds to record your peak and average millisecond reflex time."
        )
    ),
    STROOP_CLASH(
        id = "STROOP_CLASH",
        title = "Stroop Color Clash",
        subtitle = "Cognitive inhibition & focus",
        category = CognitiveCategory.FOCUS,
        description = "Overcome interference between reading word meaning and recognizing font color.",
        rules = listOf(
            "A color name is displayed in a colored font (e.g., 'BLUE' printed in Red font).",
            "Read the question prompt: identify either the FONT COLOR or the WRITTEN WORD.",
            "Tap the corresponding color button before time elapses.",
            "Build consecutive correct answer streaks for score multipliers."
        )
    ),
    MATH_BLITZ(
        id = "MATH_BLITZ",
        title = "Mental Math Blitz",
        subtitle = "Numerical agility & logic",
        category = CognitiveCategory.LOGIC,
        description = "Solve high-speed arithmetic operations under a strict countdown timer.",
        rules = listOf(
            "Quick mental calculations: addition, subtraction, multiplication.",
            "Verify whether the given formula is Correct or Incorrect, or pick the solution.",
            "Each quick correct answer adds bonus seconds to your clock.",
            "Difficulty scales arithmetic complexity as your streak rises."
        )
    ),
    NUMBER_SEQUENCE(
        id = "NUMBER_SEQUENCE",
        title = "Pattern Sequence",
        subtitle = "Deductive pattern logic",
        category = CognitiveCategory.LOGIC,
        description = "Analyze arithmetic, geometric, and alternating numeric patterns to find the missing element.",
        rules = listOf(
            "Examine the 4-5 visible numbers in the progression (e.g., 3, 6, 12, 24, ?).",
            "Determine the mathematical rule governing the progression.",
            "Select the correct missing term from 4 candidate numbers.",
            "Accuracy and speed both contribute to your cognitive index score."
        )
    ),
    DUAL_FLANKER(
        id = "DUAL_FLANKER",
        title = "Flanker Arrow Test",
        subtitle = "Selective attention & suppression",
        category = CognitiveCategory.FOCUS,
        description = "Identify the direction of the center arrow while actively ignoring conflicting flanker arrows.",
        rules = listOf(
            "A row of 5 arrows appears (e.g., <<><< or >>>>>).",
            "Focus strictly on the SINGLE CENTER ARROW.",
            "Tap LEFT or RIGHT according to which direction the center arrow points.",
            "Ignore whether the outer arrows agree or conflict with the center."
        )
    );

    companion object {
        fun fromId(id: String): GameType = entries.find { it.id == id } ?: MEMORY_MATRIX
    }
}
