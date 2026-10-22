package com.example.stepcounter.domain

import com.example.stepcounter.data.DayRecord

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val unlocked: Boolean,
    val progress: Float // 0f..1f toward unlocking
)

/**
 * Derives a fixed set of badges from the recorded history. Pure function — no
 * state stored, everything is recomputed from the stats file.
 */
object Achievements {

    fun compute(history: List<DayRecord>, stepGoal: Int): List<Achievement> {
        val totalSteps = history.sumOf { it.steps.toLong() }
        val bestDay = history.maxOfOrNull { it.steps } ?: 0
        val maxActive = history.maxOfOrNull { it.activeMinutes } ?: 0
        val goalDays = history.count { it.steps >= stepGoal }
        val bestStreak = longestGoalStreak(history, stepGoal)

        fun ratio(current: Number, target: Number): Float =
            (current.toDouble() / target.toDouble()).coerceIn(0.0, 1.0).toFloat()

        return listOf(
            Achievement(
                id = "first_steps",
                title = "Первые шаги",
                description = "Сделайте первый шаг с приложением",
                unlocked = bestDay > 0,
                progress = if (bestDay > 0) 1f else 0f
            ),
            Achievement(
                id = "goal_day",
                title = "Цель дня",
                description = "Выполните дневную цель хотя бы раз",
                unlocked = goalDays >= 1,
                progress = ratio(goalDays, 1)
            ),
            Achievement(
                id = "ten_k",
                title = "10 000 за день",
                description = "Пройдите 10 000 шагов за один день",
                unlocked = bestDay >= 10_000,
                progress = ratio(bestDay, 10_000)
            ),
            Achievement(
                id = "marathoner",
                title = "Марафонец",
                description = "20 000 шагов за один день",
                unlocked = bestDay >= 20_000,
                progress = ratio(bestDay, 20_000)
            ),
            Achievement(
                id = "active_hour",
                title = "Активный час",
                description = "60 минут активности за день",
                unlocked = maxActive >= 60,
                progress = ratio(maxActive, 60)
            ),
            Achievement(
                id = "streak_3",
                title = "Серия из 3 дней",
                description = "3 дня подряд с выполненной целью",
                unlocked = bestStreak >= 3,
                progress = ratio(bestStreak, 3)
            ),
            Achievement(
                id = "streak_7",
                title = "Неделя в строю",
                description = "7 дней подряд с выполненной целью",
                unlocked = bestStreak >= 7,
                progress = ratio(bestStreak, 7)
            ),
            Achievement(
                id = "hundred_k",
                title = "100 000 шагов",
                description = "Наберите 100 000 шагов суммарно",
                unlocked = totalSteps >= 100_000,
                progress = ratio(totalSteps, 100_000)
            ),
            Achievement(
                id = "half_million",
                title = "Полмиллиона",
                description = "500 000 шагов суммарно",
                unlocked = totalSteps >= 500_000,
                progress = ratio(totalSteps, 500_000)
            )
        )
    }

    /** Longest run of calendar-consecutive days whose steps met [stepGoal]. */
    fun longestGoalStreak(history: List<DayRecord>, stepGoal: Int): Int {
        val goalDates = history.filter { it.steps >= stepGoal }.map { it.date }.sorted()
        if (goalDates.isEmpty()) return 0
        var best = 1
        var current = 1
        for (i in 1 until goalDates.size) {
            current = if (goalDates[i - 1].plusDays(1) == goalDates[i]) current + 1 else 1
            if (current > best) best = current
        }
        return best
    }

    /** Streak counting backwards from the most recent recorded day. */
    fun currentGoalStreak(history: List<DayRecord>, stepGoal: Int): Int {
        val goalDates = history.filter { it.steps >= stepGoal }.map { it.date }.sortedDescending()
        if (goalDates.isEmpty()) return 0
        var streak = 1
        for (i in 1 until goalDates.size) {
            if (goalDates[i].plusDays(1) == goalDates[i - 1]) streak++ else break
        }
        return streak
    }
}
