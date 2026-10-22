package com.example.stepcounter.data

import java.time.LocalDate

/**
 * One day of activity. [steps] comes from the hardware sensor; [activeMinutes]
 * is time spent moving. Calories are not stored — they are derived from steps
 * (see StepMath.estimateCalories), so the file stays small and honest.
 */
data class DayRecord(
    val date: LocalDate,
    val steps: Int,
    val activeMinutes: Int
)
