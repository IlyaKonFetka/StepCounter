package com.example.stepcounter.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import kotlin.math.pow
import kotlin.math.roundToInt

data class StepUpdate(
    val lastRaw: Int,
    val stepsToday: Int
)

/**
 * Pure math for turning the raw, ever-increasing hardware step count into
 * per-day metrics.
 */
object StepMath {

    private const val METERS_PER_STEP = 0.75
    private const val STEPS_PER_ACTIVE_MINUTE = 100.0

    // Calories grow faster than steps: sustained walking burns proportionally
    // more (fatigue + afterburn). Modelled as a super-linear power curve tuned
    // so ~10 000 steps ≈ 400 kcal, and doubling the steps more than doubles the
    // calories.
    private const val CAL_COEFF = 0.00159
    private const val CAL_EXP = 1.35

    /**
     * Incremental update: adds only the steps taken since the previous sensor
     * reading ([lastRaw]) to [currentTodaySteps] (whatever today's value is in
     * the stats file right now). This makes the file the source of truth — a
     * hand-edited value becomes the new base and real steps accumulate on top of
     * it, instead of being recomputed from a fixed baseline.
     *
     * @param raw current cumulative sensor reading (steps since last reboot)
     * @param lastRaw the previous raw reading, or -1 if none yet
     * @param currentTodaySteps steps currently recorded for today in the file
     */
    fun applySensorReading(raw: Int, lastRaw: Int, currentTodaySteps: Int): StepUpdate {
        val delta = when {
            lastRaw < 0 -> 0          // first reading ever: just sync, add nothing
            raw < lastRaw -> raw      // device rebooted (counter reset): all `raw` are new
            else -> raw - lastRaw     // normal: steps since the last reading
        }
        val steps = (currentTodaySteps + delta).coerceAtLeast(0)
        return StepUpdate(lastRaw = raw, stepsToday = steps)
    }

    fun estimateDistanceKm(steps: Int): Double = steps * METERS_PER_STEP / 1000.0

    /** Non-linear: calories rise faster than steps. */
    fun estimateCalories(steps: Int): Int =
        if (steps <= 0) 0 else (CAL_COEFF * steps.toDouble().pow(CAL_EXP)).roundToInt()

    fun estimateActiveMinutes(steps: Int): Int =
        if (steps <= 0) 0 else (steps / STEPS_PER_ACTIVE_MINUTE).roundToInt()

    fun isGoalAchieved(value: Int, goal: Int): Boolean = goal > 0 && value >= goal

    /** Monday of the week that [date] falls in (week starts on Monday). */
    fun mondayOf(date: LocalDate): LocalDate =
        date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
}
