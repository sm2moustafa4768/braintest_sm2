package com.example.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ConceptGuideScreen() {
    var selectedSection by remember { mutableIntStateOf(0) }
    val sectionTitles = listOf(
        "Overview & Scope",
        "Mini-Games Spec",
        "Technical & DB",
        "Monetization",
        "Prompt Kit"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("concept_guide_screen")
    ) {
        Text(
            text = "Brain Trainer App Spec",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Complete production architecture & game concepts",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        ScrollableTabRow(
            selectedTabIndex = selectedSection,
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            sectionTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSection == index,
                    onClick = { selectedSection = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            when (selectedSection) {
                0 -> {
                    // Overview & Scope
                    item {
                        ConceptCard(
                            title = "1. App Description & Purpose",
                            content = """
Brain Trainer is a cognitive enhancement mobile application engineered to measure, train, and expand key neurocognitive functions: working memory, sustained focus, sensory-motor reaction speed, and logical reasoning.

Target Audience:
• Students seeking sharper recall and academic concentration.
• Working professionals combating mental fatigue and multitasking distractions.
• Adults aiming to maintain active neuroplasticity and cognitive vitality.

Core Value Proposition:
Bite-sized, scientifically inspired 60-second micro-challenges that adapt dynamically to individual skill levels, tracking daily progress via a unified Brain Score Index (BSI).
                            """.trimIndent()
                        )
                    }
                    item {
                        ConceptCard(
                            title = "2. UI/UX Design System",
                            content = """
Aesthetic Philosophy: "Dark Slate Cyber-Clean"
• Primary Brand Palette: Electric Indigo (#4F46E5), Neon Cyan (#06B6D4), Warning Amber (#F59E0B), Reflex Emerald (#10B981).
• Canvas: Deep Navy (#0F172A) & Crisp Surface Slate (#1E293B) to eliminate visual noise and ocular strain during intensive focus sessions.
• Touch Targets: 56dp minimum interactive size, high-contrast typography, and zero-distraction layout.
• Motion & Tactile Feedback: Responsive color transformations (animateColorAsState) for instant reflex affirmation.
                            """.trimIndent()
                        )
                    }
                }
                1 -> {
                    // Mini-Games Spec
                    item {
                        ConceptCard(
                            title = "Active Mini-Games (6 Total)",
                            content = """
1. Memory Matrix (Memory)
• Rule: 3x3 to 5x5 grid flashes random active tiles for 1.8s. Player must tap exact positions from memory.
• Progression: Grid scale and target count increase every round. 3 lives.

2. Reaction Tap (Speed)
• Rule: Screen transitions to high-tension state, then flashes bright green at a random timestamp.
• Metric: Sub-millisecond timing (120ms - 600ms range). Penalizes false triggers.

3. Stroop Color Clash (Focus & Inhibition)
• Rule: Overcome the Stroop Interference effect. Prompts toggle between matching font color vs. written word.

4. Mental Math Blitz (Logic & Numerical Agility)
• Rule: 25-second rapid arithmetic verification (True/False). Chained combos award bonus seconds.

5. Pattern Sequence (Logic & Deduction)
• Rule: Uncover algorithmic arithmetic, geometric, or alternating sequences (e.g., 3, 6, 12, 24, ?).

6. Flanker Arrow Test (Focus & Selective Attention)
• Rule: Eriksen flanker paradigm. Filter out flanking distractor arrows and tap the direction of the center target arrow.
                            """.trimIndent()
                        )
                    }
                }
                2 -> {
                    // Technical & DB
                    item {
                        ConceptCard(
                            title = "Technical Architecture (Android + Kotlin)",
                            content = """
• Architecture: MVVM + Repository Pattern + Clean Architecture principles.
• UI Framework: Jetpack Compose with Material 3 theming and state hoisting.
• Persistence: Room Database (SQLite) with KSP and reactive Kotlin Coroutine Flows.
• Asynchronous Engine: Coroutines & StateFlow for non-blocking reactive rendering.

Database Tables:
1. game_sessions (id, gameId, category, score, reactionTimeMs, accuracy, difficulty, timestamp, isDailyChallenge)
2. daily_challenges (dateKey, completedGameIds, isCompleted, xpEarned)
3. user_profile (id, username, totalXp, streakDays, lastActiveDate, brainScore)
                            """.trimIndent()
                        )
                    }
                }
                3 -> {
                    // Monetization
                    item {
                        ConceptCard(
                            title = "Monetization Strategy",
                            content = """
1. Freemium Core
• Daily Challenge and 3 standard mini-games unlocked indefinitely with zero payment barrier.
• Rewarded Video Ads: "Second Chance" extra life in Memory Matrix or 1.5x XP booster after workouts.

2. Brain Trainer Pro (Subscription / Lifetime)
• Pricing: ${'$'}4.99/month, ${'$'}29.99/year, or ${'$'}49.99 lifetime unlock.
• Pro Features:
  - Ad-free uninterrupted flow.
  - Unlimited daily workouts & all 6 games unlocked across all difficulty tiers.
  - Advanced Cognitive Analytics: Percentile comparisons against age demographics and weekly trend lines.
  - Offline Audio Immersion: Binaural focus soundscapes while training.
                            """.trimIndent()
                        )
                    }
                }
                4 -> {
                    // Prompt Kit
                    item {
                        ConceptCard(
                            title = "Generative AI Prompt Suite",
                            content = """
App Launcher Icon:
"Minimalist modern app icon of a glowing stylized human brain with neural synapses, geometric clean vector art, vibrant electric violet, indigo and cyan neon gradient accents, solid deep navy blue background, premium mobile app icon design, centered, no text"

Game Cards & UI Illustrations:
• Memory Matrix: "3D isometric glass tiles floating in dark space, one glowing neon cyan tile, clean minimalist tech render, 8k, Unreal Engine 5 aesthetic"
• Speed Reaction: "Stylized electric lightning bolt striking a circular reactive neon green target, high velocity motion blur, cyberpunk vector icon"
• Stroop Clash: "Split chromatic spectrum sphere with dual contrasting colors magenta and cyan, optical illusion aesthetic, clean geometric studio lighting"
• Math Blitz: "Glowing arithmetic symbols +, -, x orbiting a luminous crystal core, deep indigo atmosphere, modern educational illustration"
                            """.trimIndent()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConceptCard(title: String, content: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
        }
    }
}
