package com.kf7mxe.autowall.engine

import com.kf7mxe.autowall.storage.readLocalFile
import com.kf7mxe.autowall.storage.writeLocalFile
import com.lightningkite.reactive.core.Signal

enum class WallpaperTarget { HomeScreen, LockScreen, Both }

object WallpaperTargetManager {
    private const val FILE = "wallpaper_target"

    val target: Signal<WallpaperTarget> = Signal(loadTarget())

    fun setTarget(t: WallpaperTarget) {
        target.value = t
        writeLocalFile(FILE, t.name)
    }

    private fun loadTarget(): WallpaperTarget {
        val saved = readLocalFile(FILE)?.trim() ?: return WallpaperTarget.HomeScreen
        return try {
            WallpaperTarget.valueOf(saved)
        } catch (e: Exception) {
            WallpaperTarget.HomeScreen
        }
    }
}
