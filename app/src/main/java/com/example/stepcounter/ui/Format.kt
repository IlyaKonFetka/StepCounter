package com.example.stepcounter.ui

import java.util.Locale

private val RU_LOCALE: Locale = Locale.forLanguageTag("ru")

fun formatThousands(value: Int): String {
    val digits = value.toString()
    val builder = StringBuilder()
    for ((index, char) in digits.reversed().withIndex()) {
        if (index != 0 && index % 3 == 0) builder.append(' ')
        builder.append(char)
    }
    return builder.reverse().toString()
}

fun formatDistanceKm(km: Double): String = String.format(RU_LOCALE, "%.2f км", km)

/** Distance from steps, honoring configurable stride length and unit system. */
fun formatDistance(steps: Int, strideCm: Int, metric: Boolean): String {
    val meters = steps.toDouble() * strideCm / 100.0
    return if (metric) {
        String.format(RU_LOCALE, "%.2f км", meters / 1000.0)
    } else {
        String.format(RU_LOCALE, "%.2f ми", meters / 1609.344)
    }
}

fun formatActiveMinutes(minutes: Int): String =
    if (minutes >= 60) {
        val h = minutes / 60
        val m = minutes % 60
        if (m == 0) "$h ч" else "$h ч $m мин"
    } else {
        "$minutes мин"
    }
