package com.example.stepcounter.ui.home

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stepcounter.R
import com.example.stepcounter.domain.StepMath
import com.example.stepcounter.ui.StepsUiState
import com.example.stepcounter.ui.components.ActivityRings
import com.example.stepcounter.ui.components.MetricTile
import com.example.stepcounter.ui.components.OutlinedText
import com.example.stepcounter.ui.components.RingSpec
import com.example.stepcounter.ui.components.SectionCard
import com.example.stepcounter.ui.components.WeekActivityStrip
import com.example.stepcounter.ui.formatActiveMinutes
import com.example.stepcounter.ui.formatDistance
import com.example.stepcounter.ui.formatThousands
import com.example.stepcounter.ui.theme.RingActive
import com.example.stepcounter.ui.theme.RingActiveTrack
import com.example.stepcounter.ui.theme.RingCalories
import com.example.stepcounter.ui.theme.RingCaloriesTrack
import com.example.stepcounter.ui.theme.RingSteps
import com.example.stepcounter.ui.theme.RingStepsTrack
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    state: StepsUiState,
    modifier: Modifier = Modifier
) {
    if (!state.sensorAvailable) {
        NoSensorMessage(modifier = modifier.fillMaxSize())
        return
    }

    val today = remember { LocalDate.now() }
    val dateLabel = remember(today) {
        val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.forLanguageTag("ru"))
        today.format(formatter).replaceFirstChar { it.uppercase() }
    }

    val stepFraction = fraction(state.todaySteps, state.stepGoal)
    val activeFraction = fraction(state.todayActiveMinutes, state.activeGoal)
    val calorieFraction = fraction(state.todayCalories, state.calorieGoal)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Привет, ${state.userName}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (state.currentStreak > 0) {
                StreakChip(days = state.currentStreak)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Rings card
        SectionCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ActivityRings(
                    rings = listOf(
                        RingSpec(stepFraction, RingSteps, RingStepsTrack),
                        RingSpec(activeFraction, RingActive, RingActiveTrack),
                        RingSpec(calorieFraction, RingCalories, RingCaloriesTrack)
                    ),
                    modifier = Modifier.size(230.dp),
                    strokeWidth = 18.dp,
                    ringGap = 7.dp
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        OutlinedText(
                            text = formatThousands(state.todaySteps),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 64.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            outlineColor = MaterialTheme.colorScheme.surface,
                            outlineWidth = 4.dp
                        )
                        OutlinedText(
                            text = "шагов сегодня",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            outlineColor = MaterialTheme.colorScheme.surface,
                            outlineWidth = 3.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    RingLegend(
                        color = RingSteps,
                        value = formatThousands(state.todaySteps),
                        goal = "из ${formatThousands(state.stepGoal)}",
                        label = "Шаги",
                        modifier = Modifier.weight(1f)
                    )
                    RingLegend(
                        color = RingActive,
                        value = "${state.todayActiveMinutes}",
                        goal = "из ${state.activeGoal} мин",
                        label = "Активность",
                        modifier = Modifier.weight(1f)
                    )
                    RingLegend(
                        color = RingCalories,
                        value = "${state.todayCalories}",
                        goal = "из ${state.calorieGoal} ккал",
                        label = "Калории",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (StepMath.isGoalAchieved(state.todaySteps, state.stepGoal)) {
            Spacer(modifier = Modifier.height(14.dp))
            GoalAchievedBanner()
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Distance + calories quick tiles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionCard(modifier = Modifier.weight(1f)) {
                MetricTile(
                    icon = Icons.Filled.Straighten,
                    tint = RingSteps,
                    value = formatDistance(state.todaySteps, state.strideCm, state.unitsMetric),
                    label = "Дистанция"
                )
            }
            SectionCard(modifier = Modifier.weight(1f)) {
                MetricTile(
                    icon = Icons.Filled.Timer,
                    tint = RingActive,
                    value = formatActiveMinutes(state.todayActiveMinutes),
                    label = "В движении"
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Week strip
        SectionCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "Эта неделя",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                WeekActivityStrip(
                    history = state.history,
                    goal = state.stepGoal,
                    today = today
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

private fun fraction(value: Int, goal: Int): Float =
    if (goal > 0) (value.toFloat() / goal) else 0f

@Composable
private fun RingLegend(
    color: Color,
    value: String,
    goal: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = goal,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StreakChip(days: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Whatshot,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$days дн.",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun GoalAchievedBanner() {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.p3),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "Дневная цель достигнута!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Отличная работа, так держать",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NoSensorMessage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.p2),
            contentDescription = null,
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "На этом устройстве нет датчика шагов",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
