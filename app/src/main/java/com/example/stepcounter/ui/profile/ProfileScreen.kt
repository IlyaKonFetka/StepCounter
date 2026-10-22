package com.example.stepcounter.ui.profile

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stepcounter.R
import com.example.stepcounter.domain.Achievements
import com.example.stepcounter.ui.StepsUiState
import com.example.stepcounter.ui.components.IconBadge
import com.example.stepcounter.ui.components.ScreenTitle
import com.example.stepcounter.ui.components.SectionCard
import com.example.stepcounter.ui.formatActiveMinutes
import com.example.stepcounter.ui.formatDistance
import com.example.stepcounter.ui.formatThousands
import com.example.stepcounter.ui.theme.RingActive
import com.example.stepcounter.ui.theme.RingCalories
import com.example.stepcounter.ui.theme.RingSteps

@Composable
fun ProfileScreen(
    state: StepsUiState,
    onNameChange: (String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showNameDialog by remember { mutableStateOf(false) }

    val totalSteps = state.history.sumOf { it.steps.toLong() }
    val totalStepsInt = totalSteps.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    val totalActive = state.history.sumOf { it.activeMinutes }
    val daysTracked = state.history.size
    val bestStreak = Achievements.longestGoalStreak(state.history, state.stepGoal)
    val unlocked = state.achievements.count { it.unlocked }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ScreenTitle("Профиль", modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Настройки",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onOpenSettings)
                    .padding(6.dp)
                    .size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Profile header
        SectionCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.p1),
                        contentDescription = null,
                        modifier = Modifier.size(58.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.userName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$unlocked наград · $daysTracked дн. в истории",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { showNameDialog = true }
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Изменить имя",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "За всё время",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TotalCard(
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                tint = RingSteps,
                value = formatThousands(totalSteps.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()),
                label = "шагов",
                modifier = Modifier.weight(1f)
            )
            TotalCard(
                icon = Icons.Filled.Straighten,
                tint = RingSteps,
                value = formatDistance(totalStepsInt, state.strideCm, state.unitsMetric),
                label = "пройдено",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TotalCard(
                icon = Icons.Filled.Timer,
                tint = RingActive,
                value = formatActiveMinutes(totalActive),
                label = "в движении",
                modifier = Modifier.weight(1f)
            )
            TotalCard(
                icon = Icons.Filled.Whatshot,
                tint = RingCalories,
                value = "$bestStreak дн.",
                label = "лучшая серия",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        SectionCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(icon = Icons.Filled.CalendarMonth, tint = RingActive, size = 44.dp)
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Дневная цель",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${formatThousands(state.stepGoal)} шагов · ${state.activeGoal} мин · ${state.calorieGoal} ккал",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
    }

    if (showNameDialog) {
        NameDialog(
            currentName = state.userName,
            onDismiss = { showNameDialog = false },
            onConfirm = {
                onNameChange(it)
                showNameDialog = false
            }
        )
    }
}

@Composable
private fun TotalCard(
    icon: ImageVector,
    tint: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier = modifier, padding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
        Column {
            IconBadge(icon = icon, tint = tint, size = 40.dp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ваше имя") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(),
                label = { Text("Имя") }
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text.trim()) }) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
