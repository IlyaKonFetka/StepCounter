package com.example.stepcounter.data

import android.content.Context
import java.io.File
import java.time.LocalDate
import kotlin.random.Random

/**
 * The single, human-readable stats file. Example:
 *
 * ```
 * # Шагомер — статистика активности по дням
 * # Каждая строка:  ГГГГ-ММ-ДД: шаги=ЧИСЛО, минуты=ЧИСЛО
 * 2026-07-14: шаги=10214, минуты=96
 * ```
 *
 * The whole file is rewritten on every update, so hand edits to the numbers of
 * past days survive. Located in the app's external files dir so it can be read
 * and edited with any file manager or `adb pull`.
 */
class StepsFileStore(context: Context) {

    private val file: File = File(context.getExternalFilesDir(null) ?: context.filesDir, FILE_NAME)

    /** Absolute path of the stats file (shown in the built-in editor). */
    val absolutePath: String get() = file.absolutePath

    /** True the very first time the app runs (before any file is created). */
    fun isFirstRun(): Boolean = !file.exists()

    /** Raw file contents for the built-in editor. */
    fun readRaw(): String = if (file.exists()) file.readText() else ""

    /** Overwrites the file verbatim with [text] (used by the built-in editor). */
    fun writeRaw(text: String) {
        file.parentFile?.mkdirs()
        file.writeText(text)
    }

    fun readAll(): List<DayRecord> =
        if (file.exists()) {
            file.readLines().mapNotNull(::parseLine).sortedBy { it.date }
        } else {
            emptyList()
        }

    fun readDay(date: LocalDate): DayRecord? = readAll().firstOrNull { it.date == date }

    fun upsertDay(date: LocalDate, steps: Int, activeMinutes: Int) {
        val records = readAll().associateBy { it.date }.toMutableMap()
        records[date] = DayRecord(date, steps, activeMinutes)
        writeAll(records.values.sortedBy { it.date })
    }

    fun ensureDateExists(date: LocalDate) {
        if (readAll().none { it.date == date }) {
            upsertDay(date, 0, 0)
        }
    }

    /** Removes the whole stats file (used by "reset history" in settings). */
    fun deleteAll() {
        if (file.exists()) file.delete()
    }

    /**
     * Populates a week of realistic sample days on first launch so charts,
     * streaks and achievements have something to show. Only past days are
     * seeded; today is always the real, sensor-driven value. Delete the file
     * to wipe this sample data.
     */
    fun seedSampleDataIfEmpty(today: LocalDate) {
        if (!file.exists()) {
            val sample = (7 downTo 1).map { daysAgo ->
                val date = today.minusDays(daysAgo.toLong())
                val steps = Random.nextInt(5200, 13400)
                DayRecord(date, steps, steps / 100 + Random.nextInt(-5, 6))
            }
            writeAll(sample)
        }
    }

    private fun parseLine(rawLine: String): DayRecord? {
        val line = rawLine.trim()
        if (line.isEmpty() || line.startsWith("#")) return null
        val separatorIndex = line.indexOf(':')
        if (separatorIndex <= 0) return null
        val datePart = line.substring(0, separatorIndex).trim()
        val valuesPart = line.substring(separatorIndex + 1)

        val date = try {
            LocalDate.parse(datePart)
        } catch (e: Exception) {
            return null
        }

        var steps = 0
        var minutes = 0
        for (token in valuesPart.split(',')) {
            val eq = token.indexOf('=')
            if (eq <= 0) continue
            val key = token.substring(0, eq).trim().lowercase()
            val value = token.substring(eq + 1).trim().filter { it.isDigit() }.toIntOrNull() ?: continue
            when {
                key.startsWith("шаг") || key.startsWith("step") -> steps = value
                key.startsWith("мин") || key.startsWith("min") || key.startsWith("актив") -> minutes = value
            }
        }
        return DayRecord(date, steps, minutes)
    }

    private fun writeAll(records: List<DayRecord>) {
        val text = buildString {
            append(HEADER)
            records.forEach { r ->
                append(r.date).append(": шаги=").append(r.steps).append(", минуты=").append(r.activeMinutes).append('\n')
            }
        }
        file.parentFile?.mkdirs()
        file.writeText(text)
    }

    private companion object {
        const val FILE_NAME = "steps_data.txt"
        val HEADER = buildString {
            appendLine("# Шагомер — статистика активности по дням")
            appendLine("# Каждая строка:  ГГГГ-ММ-ДД: шаги=ЧИСЛО, минуты=ЧИСЛО")
            appendLine("# Строки с # и пустые строки игнорируются. Значения можно менять вручную.")
            appendLine()
        }
    }
}
