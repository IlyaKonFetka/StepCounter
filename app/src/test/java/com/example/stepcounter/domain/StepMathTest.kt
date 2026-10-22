package com.example.stepcounter.domain

import java.time.DayOfWeek
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StepMathTest {

    private val today = LocalDate.of(2026, 7, 18)

    @Test
    fun `first ever reading only synchronizes the raw counter`() {
        val result = StepMath.applySensorReading(
            raw = 1234,
            lastRaw = -1,
            currentTodaySteps = 0
        )
        assertEquals(1234, result.lastRaw)
        assertEquals(0, result.stepsToday)
    }

    @Test
    fun `sensor delta is added to the value currently stored for today`() {
        val result = StepMath.applySensorReading(
            raw = 5500,
            lastRaw = 5300,
            currentTodaySteps = 300
        )
        assertEquals(5500, result.lastRaw)
        assertEquals(500, result.stepsToday)
    }

    @Test
    fun `real step continues from a manually edited file value`() {
        val result = StepMath.applySensorReading(
            raw = 5501,
            lastRaw = 5500,
            currentTodaySteps = 1200
        )
        assertEquals(1201, result.stepsToday)
    }

    @Test
    fun `device reboot adds steps counted after the reboot`() {
        val result = StepMath.applySensorReading(
            raw = 40,
            lastRaw = 5300,
            currentTodaySteps = 300
        )
        assertEquals(40, result.lastRaw)
        assertEquals(340, result.stepsToday)
    }

    @Test
    fun `steps never go negative`() {
        val result = StepMath.applySensorReading(
            raw = 100,
            lastRaw = -1,
            currentTodaySteps = -10
        )
        assertEquals(0, result.stepsToday)
    }

    @Test
    fun `distance estimate scales linearly with steps`() {
        assertEquals(0.75, StepMath.estimateDistanceKm(1000), 0.0001)
        assertEquals(0.0, StepMath.estimateDistanceKm(0), 0.0001)
    }

    @Test
    fun `calories grow non-linearly - doubling steps more than doubles calories`() {
        val cal10k = StepMath.estimateCalories(10_000)
        val cal20k = StepMath.estimateCalories(20_000)
        assertTrue("calories should be positive", cal10k > 0)
        assertTrue("doubling steps should more than double calories", cal20k > cal10k * 2)
    }

    @Test
    fun `active minutes derived from steps`() {
        assertEquals(0, StepMath.estimateActiveMinutes(0))
        assertEquals(100, StepMath.estimateActiveMinutes(10_000))
    }

    @Test
    fun `mondayOf returns the monday of the week`() {
        // 2026-07-18 is a Saturday
        val monday = StepMath.mondayOf(today)
        assertEquals(DayOfWeek.MONDAY, monday.dayOfWeek)
        assertEquals(LocalDate.of(2026, 7, 13), monday)
        // A Monday maps to itself
        assertEquals(monday, StepMath.mondayOf(monday))
    }

    @Test
    fun `goal achieved only when value reaches goal`() {
        assertTrue(StepMath.isGoalAchieved(10_000, 10_000))
        assertTrue(!StepMath.isGoalAchieved(9_999, 10_000))
    }
}
