package com.example.stepcounter.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stepcounter.data.DayRecord
import com.example.stepcounter.data.StepPrefs
import com.example.stepcounter.data.StepsFileStore
import com.example.stepcounter.domain.Achievement
import com.example.stepcounter.domain.Achievements
import com.example.stepcounter.domain.StepMath
import com.example.stepcounter.sensor.StepCounterSensor
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

data class StepsUiState(
    val sensorAvailable: Boolean = true,
    val todaySteps: Int = 0,
    val todayActiveMinutes: Int = 0,
    val todayCalories: Int = 0,
    val stepGoal: Int = StepPrefs.DEFAULT_STEP_GOAL,
    val activeGoal: Int = StepPrefs.DEFAULT_ACTIVE_GOAL,
    val calorieGoal: Int = StepPrefs.DEFAULT_CALORIE_GOAL,
    val userName: String = StepPrefs.DEFAULT_NAME,
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val unitsMetric: Boolean = true,
    val strideCm: Int = StepPrefs.DEFAULT_STRIDE_CM,
    val history: List<DayRecord> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val currentStreak: Int = 0,
    val isLoading: Boolean = true
)

class StepsViewModel(application: Application) : AndroidViewModel(application) {

    private val fileStore = StepsFileStore(application)
    private val prefs = StepPrefs(application)
    private val sensor = StepCounterSensor(application)
    private val updateMutex = Mutex()

    private val _uiState = MutableStateFlow(
        StepsUiState(
            sensorAvailable = sensor.isAvailable(),
            stepGoal = prefs.stepGoal,
            activeGoal = prefs.activeGoal,
            calorieGoal = prefs.calorieGoal,
            userName = prefs.userName,
            themeMode = ThemeMode.fromKey(prefs.themeMode),
            unitsMetric = prefs.unitsMetric,
            strideCm = prefs.strideCm
        )
    )
    val uiState: StateFlow<StepsUiState> = _uiState.asStateFlow()

    fun start() {
        val today = LocalDate.now()
        // Seed + ensure the file BEFORE registering the sensor, otherwise the
        // sensor's first callback can race the seeding and create the file first
        // (skipping the sample week).
        viewModelScope.launch(Dispatchers.IO) {
            updateMutex.withLock {
                fileStore.seedSampleDataIfEmpty(today)
                fileStore.ensureDateExists(today)
            }
            refresh()
            if (sensor.isAvailable()) {
                sensor.start(::onRawSteps)
            }
        }
    }

    fun stop() {
        sensor.stop()
    }

    fun setStepGoal(goal: Int) {
        prefs.stepGoal = goal.coerceAtLeast(1)
        viewModelScope.launch(Dispatchers.IO) { refresh() }
    }

    fun setActiveGoal(goal: Int) {
        prefs.activeGoal = goal.coerceAtLeast(1)
        viewModelScope.launch(Dispatchers.IO) { refresh() }
    }

    fun setCalorieGoal(goal: Int) {
        prefs.calorieGoal = goal.coerceAtLeast(1)
        viewModelScope.launch(Dispatchers.IO) { refresh() }
    }

    fun setUserName(name: String) {
        prefs.userName = name.ifBlank { StepPrefs.DEFAULT_NAME }
        _uiState.update { it.copy(userName = prefs.userName) }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.themeMode = mode.storageKey
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setUnitsMetric(metric: Boolean) {
        prefs.unitsMetric = metric
        _uiState.update { it.copy(unitsMetric = metric) }
    }

    fun setStrideCm(cm: Int) {
        prefs.strideCm = cm.coerceIn(40, 120)
        _uiState.update { it.copy(strideCm = prefs.strideCm) }
    }

    /** Absolute path of the stats file, shown in the built-in editor. */
    val statsFilePath: String get() = fileStore.absolutePath

    /** Reads the raw file contents for the built-in editor. */
    suspend fun readRawFile(): String = withContext(Dispatchers.IO) { fileStore.readRaw() }

    /** Saves raw text typed in the built-in editor, then refreshes the UI. */
    fun saveRawFile(text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            updateMutex.withLock { fileStore.writeRaw(text) }
            refresh()
        }
    }

    /** Clears the whole stats file and the sensor baseline (today restarts at 0). */
    fun resetHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            updateMutex.withLock {
                val today = LocalDate.now()
                fileStore.deleteAll()
                prefs.resetBaseline()
                fileStore.ensureDateExists(today)
            }
            refresh()
        }
    }

    private fun onRawSteps(raw: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            updateMutex.withLock {
                val today = LocalDate.now()
                val recordedToday = fileStore.readDay(today)?.steps ?: 0
                val result = StepMath.applySensorReading(
                    raw = raw,
                    lastRaw = prefs.lastRawSteps,
                    currentTodaySteps = recordedToday
                )
                prefs.lastRawSteps = result.lastRaw
                fileStore.upsertDay(
                    date = today,
                    steps = result.stepsToday,
                    activeMinutes = StepMath.estimateActiveMinutes(result.stepsToday)
                )
            }
            refresh()
        }
    }

    private suspend fun refresh() {
        val today = LocalDate.now()
        val records = withContext(Dispatchers.IO) { fileStore.readAll() }
        val todayRecord = records.firstOrNull { it.date == today }
        val steps = todayRecord?.steps ?: 0
        val stepGoal = prefs.stepGoal
        _uiState.update {
            it.copy(
                todaySteps = steps,
                todayActiveMinutes = todayRecord?.activeMinutes ?: StepMath.estimateActiveMinutes(steps),
                todayCalories = StepMath.estimateCalories(steps),
                stepGoal = stepGoal,
                activeGoal = prefs.activeGoal,
                calorieGoal = prefs.calorieGoal,
                userName = prefs.userName,
                themeMode = ThemeMode.fromKey(prefs.themeMode),
                unitsMetric = prefs.unitsMetric,
                strideCm = prefs.strideCm,
                history = records,
                achievements = Achievements.compute(records, stepGoal),
                currentStreak = Achievements.currentGoalStreak(records, stepGoal),
                isLoading = false
            )
        }
    }
}
