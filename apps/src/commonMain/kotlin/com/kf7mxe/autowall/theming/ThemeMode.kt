package com.kf7mxe.autowall.theming

import com.kf7mxe.autowall.storage.readLocalFile
import com.kf7mxe.autowall.storage.writeLocalFile
import com.lightningkite.reactive.core.Signal

enum class ThemeMode { Auto, Light, Dark }

object ThemeModeManager {
    private const val FILE = "theme_mode"

    val mode: Signal<ThemeMode> = Signal(loadMode())

    fun setMode(m: ThemeMode) {
        mode.value = m
        writeLocalFile(FILE, m.name)
    }

    private fun loadMode(): ThemeMode {
        val saved = readLocalFile(FILE)?.trim() ?: return ThemeMode.Auto
        return try {
            ThemeMode.valueOf(saved)
        } catch (e: Exception) {
            ThemeMode.Auto
        }
    }
}
