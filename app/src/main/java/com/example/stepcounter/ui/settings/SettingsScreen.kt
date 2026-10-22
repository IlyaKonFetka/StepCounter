package com.example.stepcounter.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stepcounter.ui.ThemeMode
import com.example.stepcounter.ui.components.IconBadge
import com.example.stepcounter.ui.components.SectionCard
import com.example.stepcounter.ui.formatDistance
import com.example.stepcounter.ui.theme.RingActive
import com.example.stepcounter.ui.theme.RingCalories
import com.example.stepcounter.ui.theme.RingSteps
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    unitsMetric: Boolean,
    strideCm: Int,
    onThemeMode: (ThemeMode) -> Unit,
    onUnitsMetric: (Boolean) -> Unit,
    onStrideCm: (Int) -> Unit,
    onReset: () -> Unit,
    onOpenEditor: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var secretTaps by remember { mutableIntStateOf(0) }
    val editorUnlocked = secretTaps >= 7

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onBack)
                    .padding(4.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Настройки",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { if (!editorUnlocked) secretTaps++ }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Theme
        SettingCard(icon = Icons.Filled.DarkMode, tint = RingActive, title = "Тема оформления") {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ThemeMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = themeMode == mode,
                        onClick = { onThemeMode(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size)
                    ) { Text(mode.label) }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Units
        SettingCard(icon = Icons.Filled.Straighten, tint = RingSteps, title = "Единицы расстояния") {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = unitsMetric,
                    onClick = { onUnitsMetric(true) },
                    shape = SegmentedButtonDefaults.itemShape(0, 2)
                ) { Text("Километры") }
                SegmentedButton(
                    selected = !unitsMetric,
                    onClick = { onUnitsMetric(false) },
                    shape = SegmentedButtonDefaults.itemShape(1, 2)
                ) { Text("Мили") }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Stride
        StrideCard(
            strideCm = strideCm,
            unitsMetric = unitsMetric,
            onStrideCm = onStrideCm
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Reset
        SettingCard(icon = Icons.Filled.RestartAlt, tint = RingCalories, title = "Сбросить историю") {
            Text(
                text = "Удаляет все записи из файла статистики. Отменить нельзя.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { showResetDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) { Text("Сбросить историю") }
        }

        // Hidden: tap the "Настройки" title 7 times to reveal the raw-file editor.
        if (editorUnlocked) {
            Spacer(modifier = Modifier.height(14.dp))
            SettingCard(icon = Icons.Filled.Code, tint = RingActive, title = "Редактор файла") {
                Text(
                    text = "Скрытый режим. Открывает и редактирует steps_data.txt прямо в приложении.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onOpenEditor) { Text("Открыть редактор") }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Сбросить историю?") },
            text = { Text("Все сохранённые дни будут удалены, а сегодняшний счётчик начнётся заново.") },
            confirmButton = {
                TextButton(onClick = {
                    onReset()
                    showResetDialog = false
                }) { Text("Сбросить") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun SettingCard(
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    title: String,
    content: @Composable () -> Unit
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(icon = icon, tint = tint, size = 40.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun StrideCard(
    strideCm: Int,
    unitsMetric: Boolean,
    onStrideCm: (Int) -> Unit
) {
    var value by remember(strideCm) { mutableFloatStateOf(strideCm.toFloat()) }
    val snapped = value.roundToInt()

    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(icon = Icons.AutoMirrored.Filled.DirectionsWalk, tint = RingSteps, size = 40.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Длина шага",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Влияет на расчёт расстояния",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "$snapped см",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RingSteps
                )
            }
            Slider(
                value = value,
                onValueChange = { value = it },
                onValueChangeFinished = { onStrideCm(snapped) },
                valueRange = 40f..120f,
                steps = 79,
                colors = SliderDefaults.colors(
                    thumbColor = RingSteps,
                    activeTrackColor = RingSteps,
                    inactiveTrackColor = RingSteps.copy(alpha = 0.2f)
                )
            )
            Text(
                text = "Пример: 1000 шагов ≈ ${formatDistance(1000, snapped, unitsMetric)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
