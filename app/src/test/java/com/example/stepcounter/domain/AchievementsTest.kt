package com.example.stepcounter.domain

import com.example.stepcounter.data.DayRecord
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementsTest {

    private val start = LocalDate.of(2026, 7, 10)

    private fun day(offset: Int, steps: Int) = DayRecord(start.plusDays(offset.toLong()), steps, steps / 100)

    @Test
    fun `longest streak counts consecutive goal days`() {
        val history = listOf(
            day(0, 12000), // goal
            day(1, 11000), // goal
            day(2, 3000),  // miss
            day(3, 10500), // goal
            day(4, 10500), // goal
            day(5, 10500)  // goal -> streak of 3
        )
        assertEquals(3, Achievements.longestGoalStreak(history, 10_000))
    }

    @Test
    fun `current streak counts back from latest day`() {
        val history = listOf(
            day(0, 3000),
            day(1, 10500),
            day(2, 10500)
        )
        assertEquals(2, Achievements.currentGoalStreak(history, 10_000))
    }

    @Test
    fun `total-based achievement unlocks and reports progress`() {
        val history = listOf(day(0, 60_000), day(1, 60_000))
        val achievements = Achievements.compute(history, 10_000)
        val hundredK = achievements.first { it.id == "hundred_k" }
        assertTrue(hundredK.unlocked) // 120k >= 100k
        val halfMillion = achievements.first { it.id == "half_million" }
        assertTrue(!halfMillion.unlocked)
        assertEquals(120_000.0 / 500_000.0, halfMillion.progress.toDouble(), 0.001)
    }
}
