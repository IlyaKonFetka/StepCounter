package com.example.stepcounter.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stepcounter.data.DayRecord
import com.example.stepcounter.domain.StepMath
import com.example.stepcounter.ui.StepsUiState
import com.example.stepcounter.ui.components.BarChartEntry
import com.example.stepcounter.ui.components.ScreenTitle
import com.example.stepcounter.ui.components.SectionCard
import com.example.stepcounter.ui.components.StepsBarChart
import com.example.stepcounter.ui.formatActiveMinutes
import com.example.stepcounter.ui.formatDistance
import com.example.stepcounter.ui.formatThousands
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun StatsScreen(
    state: StepsUiState,
    modifier: Modifier = Modifier
) {
    val history = state.history
    val sortedAsc = remember(history) { history.sortedBy { it.date } }
    val sortedDesc = remember(sortedAsc) { sortedAsc.asReversed() }

    val totalSteps = sortedAsc.sumOf { it.steps.toLong() }
    val avgSteps = if (sortedAsc.isNotEmpty()) (totalSteps / sortedAsc.size).toInt() else 0
    val bestDay = sortedAsc.maxOfOrNull { it.steps } ?: 0

    val chartEntries = remember(sortedAsc) {
        sortedAsc.takeLast(14).map { record ->
            BarChartEntry(
                label = record.date.format(DateTimeFormatter.ofPattern("d.MM")),
                value = record.steps
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        ScreenTitle("Статистика")
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryTile("Среднее", "${formatThousands(avgSteps)}", "шагов/день", Modifier.weight(1f))
            SummaryTile("Рекорд", "${formatThousands(bestDay)}", "за день", Modifier.weight(1f))
            SummaryTile("Всего", compactTotal(totalSteps), "шагов", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(14.dp))

        SectionCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "Шаги за 2 недели",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (chartEntries.isEmpty()) {
                    Text(
                        text = "Пока нет данных",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    StepsBarChart(entries = chartEntries, goal = state.stepGoal)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "По дням",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        sortedDesc.forEach { record ->
            DayRow(
                record = record,
                goal = state.stepGoal,
                strideCm = state.strideCm,
                metric = state.unitsMetric
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

private fun compactTotal(total: Long): String =
    if (total >= 1000) String.format(Locale.forLanguageTag("ru"), "%.1fк", total / 1000.0)
    else total.toString()

@Composable
private fun SummaryTile(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier = modifier, padding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DayRow(record: DayRecord, goal: Int, strideCm: Int, metric: Boolean) {
    val achieved = StepMath.isGoalAchieved(record.steps, goal)
    SectionCard(
        modifier = Modifier.fillMaxWidth(),
        padding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = record.date.format(
                        DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("ru"))
                    ).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${formatDistance(record.steps, strideCm, metric)} · " +
                        "${formatActiveMinutes(record.activeMinutes)} · " +
                        "${StepMath.estimateCalories(record.steps)} ккал",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = formatThousands(record.steps),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (achieved) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
