package com.example.stepcounter.data

import android.content.Context
import java.time.LocalDate

/**
 * Technical bookkeeping (sensor baseline for converting the raw cumulative
 * count into "today"), plus user settings: three daily goals and a profile
 * name. This is not part of the human-readable stats file.
 */
class StepPrefs(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var baselineDate: LocalDate?
        get() = prefs.getString(KEY_BASELINE_DATE, null)?.let { LocalDate.parse(it) }
        set(value) = prefs.edit().putString(KEY_BASELINE_DATE, value?.toString()).apply()

    var baselineRawSteps: Int
        get() = prefs.getInt(KEY_BASELINE_RAW, NO_VALUE)
        set(value) = prefs.edit().putInt(KEY_BASELINE_RAW, value).apply()

    var lastRawSteps: Int
        get() = prefs.getInt(KEY_LAST_RAW, NO_VALUE)
        set(value) = prefs.edit().putInt(KEY_LAST_RAW, value).apply()

    var stepGoal: Int
        get() = prefs.getInt(KEY_STEP_GOAL, DEFAULT_STEP_GOAL)
        set(value) = prefs.edit().putInt(KEY_STEP_GOAL, value).apply()

    var activeGoal: Int
        get() = prefs.getInt(KEY_ACTIVE_GOAL, DEFAULT_ACTIVE_GOAL)
        set(value) = prefs.edit().putInt(KEY_ACTIVE_GOAL, value).apply()

    var calorieGoal: Int
        get() = prefs.getInt(KEY_CALORIE_GOAL, DEFAULT_CALORIE_GOAL)
        set(value) = prefs.edit().putInt(KEY_CALORIE_GOAL, value).apply()

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, DEFAULT_NAME) ?: DEFAULT_NAME
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    /** "LIGHT" | "DARK" | "SYSTEM" */
    var themeMode: String
        get() = prefs.getString(KEY_THEME, DEFAULT_THEME) ?: DEFAULT_THEME
        set(value) = prefs.edit().putString(KEY_THEME, value).apply()

    var unitsMetric: Boolean
        get() = prefs.getBoolean(KEY_UNITS_METRIC, true)
        set(value) = prefs.edit().putBoolean(KEY_UNITS_METRIC, value).apply()

    var strideCm: Int
        get() = prefs.getInt(KEY_STRIDE_CM, DEFAULT_STRIDE_CM)
        set(value) = prefs.edit().putInt(KEY_STRIDE_CM, value).apply()

    /** Wipes the sensor baseline so today's count restarts from zero. */
    fun resetBaseline() {
        prefs.edit()
            .remove(KEY_BASELINE_DATE)
            .putInt(KEY_BASELINE_RAW, NO_VALUE)
            .putInt(KEY_LAST_RAW, NO_VALUE)
            .apply()
    }

    companion object {
        const val DEFAULT_STEP_GOAL = 10_000
        const val DEFAULT_ACTIVE_GOAL = 60
        const val DEFAULT_CALORIE_GOAL = 450
        const val DEFAULT_NAME = "Пользователь"
        const val DEFAULT_THEME = "LIGHT"
        const val DEFAULT_STRIDE_CM = 75
        const val NO_VALUE = -1
        private const val PREFS_NAME = "step_counter_prefs"
        private const val KEY_BASELINE_DATE = "baseline_date"
        private const val KEY_BASELINE_RAW = "baseline_raw"
        private const val KEY_LAST_RAW = "last_raw"
        private const val KEY_STEP_GOAL = "step_goal"
        private const val KEY_ACTIVE_GOAL = "active_goal"
        private const val KEY_CALORIE_GOAL = "calorie_goal"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_UNITS_METRIC = "units_metric"
        private const val KEY_STRIDE_CM = "stride_cm"
    }
}
