package com.dynamicwallpaper.engine

import android.service.quicksettings.TileService
import com.dynamicwallpaper.RandomInPlaylist
import com.dynamicwallpaper.storage.AndroidContext
import com.dynamicwallpaper.storage.LocalPlaylistStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShuffleWallpaperTileService : TileService() {
    override fun onClick() {
        super.onClick()
        AndroidContext.appContext = applicationContext
        val playlistId = LocalPlaylistStore.activePlaylistId.value ?: return
        CoroutineScope(Dispatchers.IO).launch {
            WallpaperActionRunner.executeAction(playlistId, RandomInPlaylist)
        }
    }
}
