package com.dynamicwallpaper.engine

import com.dynamicwallpaper.Action
import com.dynamicwallpaper.storage.LocalPlaylistStore
import com.dynamicwallpaper.theming.DynamicThemeManager
import kotlin.uuid.Uuid

object WallpaperActionRunner {

    suspend fun executeAction(playlistId: Uuid, action: Action) {
        val playlist = LocalPlaylistStore.getById(playlistId) ?: return
        val updated = PlaylistEngine.executeAction(playlist, action)
        LocalPlaylistStore.save(updated)
        val imageId = PlaylistEngine.getCurrentImageId(updated) ?: return
        setWallpaper(playlistId.toString(), imageId)
        if (DynamicThemeManager.isEnabled.value) {
            DynamicThemeManager.updateThemeFromCurrentWallpaper()
        }
    }

    suspend fun applyCurrentWallpaper(playlistId: Uuid) {
        val playlist = LocalPlaylistStore.getById(playlistId) ?: return
        val imageId = PlaylistEngine.getCurrentImageId(playlist) ?: return
        setWallpaper(playlistId.toString(), imageId)
        if (DynamicThemeManager.isEnabled.value) {
            DynamicThemeManager.updateThemeFromCurrentWallpaper()
        }
    }
}
