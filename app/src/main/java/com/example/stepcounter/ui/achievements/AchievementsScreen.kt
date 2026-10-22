package com.example.stepcounter.ui.achievements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.stepcounter.domain.Achievement
import com.example.stepcounter.ui.StepsUiState
import com.example.stepcounter.ui.components.IconBadge
import com.example.stepcounter.ui.components.ScreenTitle
import com.example.stepcounter.ui.components.SectionCard
import com.example.stepcounter.ui.theme.RingCalories
import com.example.stepcounter.ui.theme.RingSteps
import kotlin.math.roundToInt

private fun iconFor(id: String): ImageVector = when (id) {
    "first_steps" -> Icons.AutoMirrored.Filled.DirectionsWalk
    "goal_day" -> Icons.Filled.CheckCircle
    "ten_k" -> Icons.Filled.Bolt
    "marathoner" -> Icons.AutoMirrored.Filled.DirectionsRun
    "active_hour" -> Icons.Filled.Timer
    "streak_3" -> Icons.Filled.Whatshot
    "streak_7" -> Icons.Filled.LocalFireDepartment
    "hundred_k" -> Icons.Filled.EmojiEvents
    "half_million" -> Icons.Filled.MilitaryTech
    else -> Icons.Filled.EmojiEvents
}

@Composable
fun AchievementsScreen(
    state: StepsUiState,
    modifier: Modifier = Modifier
) {
    val achievements = state.achievements
    val unlockedCount = achievements.count { it.unlocked }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        ScreenTitle("Награды")
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Открыто $unlockedCount из ${achievements.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        achievements.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                rowItems.forEach { achievement ->
                    AchievementCard(achievement, Modifier.weight(1f))
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun AchievementCard(achievement: Achievement, modifier: Modifier = Modifier) {
    val tint = if (achievement.unlocked) RingSteps else MaterialTheme.colorScheme.onSurfaceVariant
    SectionCard(modifier = modifier, padding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            if (achievement.unlocked) {
                IconBadge(icon = iconFor(achievement.id), tint = tint, size = 54.dp)
            } else {
                Box(contentAlignment = Alignment.Center) {
                    IconBadge(icon = iconFor(achievement.id), tint = tint, size = 54.dp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            if (achievement.unlocked) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = RingSteps,
                        modifier = Modifier.height(16.dp)
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = " Получено",
                        style = MaterialTheme.typography.labelSmall,
                        color = RingSteps,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                LinearProgressIndicator(
                    progress = { achievement.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = RingCalories,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${(achievement.progress * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
