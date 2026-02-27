package com.dynamicwallpaper.theming

import com.dynamicwallpaper.appTheme
import com.dynamicwallpaper.defaultTheme
import com.dynamicwallpaper.engine.PlaylistEngine
import com.dynamicwallpaper.storage.LocalPlaylistStore
import com.dynamicwallpaper.storage.readLocalFile
import com.dynamicwallpaper.storage.writeLocalFile
import com.lightningkite.reactive.core.Signal

private const val ENABLED_FILE = "dynamic_theme_enabled.json"
private const val COLORS_FILE = "theme_colors.json"

object DynamicThemeManager {

    val isEnabled: Signal<Boolean> = Signal(loadEnabled())

    suspend fun updateThemeFromCurrentWallpaper() {
        val playlistId = LocalPlaylistStore.activePlaylistId.value ?: return
        val playlist = LocalPlaylistStore.getById(playlistId) ?: return
        val imageId = PlaylistEngine.getCurrentImageId(playlist) ?: return

        val pixels = loadImagePixels(playlistId.toString(), imageId) ?: return
        val colors = ColorQuantizer.quantize(pixels.pixels, pixels.width, pixels.height, 5)
        if (colors.isEmpty()) return

        val theme = DynamicThemeGenerator.generateTheme(colors)
        appTheme.value = theme

        // Persist extracted colors
        persistColors(colors)
    }

    fun applyTheme() {
        if (!isEnabled.value) return
        val colors = loadPersistedColors()
        if (colors.isEmpty()) return
        val theme = DynamicThemeGenerator.generateTheme(colors)
        appTheme.value = theme
    }

    fun resetToDefault() {
        appTheme.value = defaultTheme
        writeLocalFile(COLORS_FILE, "")
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled.value = enabled
        writeLocalFile(ENABLED_FILE, if (enabled) "true" else "false")
    }

    private fun loadEnabled(): Boolean {
        val raw = readLocalFile(ENABLED_FILE)?.trim() ?: return false
        return raw == "true"
    }

    private fun persistColors(colors: List<RgbColor>) {
        // Simple CSV format: r,g,b;r,g,b;...
        val data = colors.joinToString(";") { "${it.r},${it.g},${it.b}" }
        writeLocalFile(COLORS_FILE, data)
    }

    private fun loadPersistedColors(): List<RgbColor> {
        val raw = readLocalFile(COLORS_FILE)?.trim() ?: return emptyList()
        if (raw.isEmpty()) return emptyList()
        return try {
            raw.split(";").map { entry ->
                val parts = entry.split(",")
                RgbColor(parts[0].toFloat(), parts[1].toFloat(), parts[2].toFloat())
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
