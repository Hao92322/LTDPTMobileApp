package com.example.todolist.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Holds app-wide settings: dark mode + language.
 * Provided via LocalAppState CompositionLocal so any screen can read/write.
 */
class AppState {
    var isDarkMode by mutableStateOf(false)
    var language by mutableStateOf("vi") // "vi" | "en"
}

val LocalAppState = compositionLocalOf { AppState() }
