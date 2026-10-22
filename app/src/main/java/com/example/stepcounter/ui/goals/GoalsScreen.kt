package com.example.stepcounter.ui.goals

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stepcounter.ui.StepsUiState
import com.example.stepcounter.ui.components.IconBadge
import com.example.stepcounter.ui.components.ScreenTitle
import com.example.stepcounter.ui.components.SectionCard
import com.example.stepcounter.ui.formatThousands
import com.example.stepcounter.ui.theme.RingActive
import com.example.stepcounter.ui.theme.RingCalories
import com.example.stepcounter.ui.theme.RingSteps
import kotlin.math.roundToInt

@Composable
fun GoalsScreen(
    state: StepsUiState,
    onStepGoal: (Int) -> Unit,
    onActiveGoal: (Int) -> Unit,
    onCalorieGoal: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        ScreenTitle("Цели")
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Настройте дневные цели под свой ритм",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        GoalCard(
            icon = Icons.AutoMirrored.Filled.DirectionsWalk,
            tint = RingSteps,
            title = "Шаги",
            valueLabel = { formatThousands(it) },
            unit = "шагов",
            current = state.todaySteps,
            goal = state.stepGoal,
            range = 3000f..25000f,
            step = 500,
            onGoal = onStepGoal
        )
        Spacer(modifier = Modifier.height(14.dp))

        GoalCard(
            icon = Icons.Filled.Timer,
            tint = RingActive,
            title = "Активность",
            valueLabel = { "$it" },
            unit = "минут",
            current = state.todayActiveMinutes,
            goal = state.activeGoal,
            range = 15f..180f,
            step = 5,
            onGoal = onActiveGoal
        )
        Spacer(modifier = Modifier.height(14.dp))

        GoalCard(
            icon = Icons.Filled.LocalFireDepartment,
            tint = RingCalories,
            title = "Калории",
            valueLabel = { "$it" },
            unit = "ккал",
            current = state.todayCalories,
            goal = state.calorieGoal,
            range = 150f..1200f,
            step = 25,
            onGoal = onCalorieGoal
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun GoalCard(
    icon: ImageVector,
    tint: Color,
    title: String,
    valueLabel: (Int) -> String,
    unit: String,
    current: Int,
    goal: Int,
    range: ClosedFloatingPointRange<Float>,
    step: Int,
    onGoal: (Int) -> Unit
) {
    var sliderValue by remember(goal) { mutableFloatStateOf(goal.toFloat()) }
    val snapped = (sliderValue / step).roundToInt() * step
    val steps = ((range.endInclusive - range.start) / step).toInt() - 1
    val progress = if (goal > 0) (current.toFloat() / goal).coerceIn(0f, 1f) else 0f

    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(icon = icon, tint = tint, size = 44.dp)
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Сегодня: ${valueLabel(current)} $unit",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = valueLabel(snapped),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = tint
                    )
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                onValueChangeFinished = { onGoal(snapped) },
                valueRange = range,
                steps = steps.coerceAtLeast(0),
                colors = SliderDefaults.colors(
                    thumbColor = tint,
                    activeTrackColor = tint,
                    inactiveTrackColor = tint.copy(alpha = 0.2f)
                )
            )
            Text(
                text = "Прогресс сегодня: ${(progress * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
