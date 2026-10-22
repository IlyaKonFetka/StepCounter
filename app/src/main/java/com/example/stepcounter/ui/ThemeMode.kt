package com.example.stepcounter.ui

enum class ThemeMode(val storageKey: String, val label: String) {
    LIGHT("LIGHT", "Светлая"),
    DARK("DARK", "Тёмная"),
    SYSTEM("SYSTEM", "Системная");

    companion object {
        fun fromKey(key: String): ThemeMode =
            entries.firstOrNull { it.storageKey == key } ?: LIGHT
    }
}
