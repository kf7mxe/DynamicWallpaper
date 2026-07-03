package com.kf7mxe.autowall.engine

import android.service.quicksettings.TileService
import com.kf7mxe.autowall.PreviousInPlaylistAction
import com.kf7mxe.autowall.storage.AndroidContext
import com.kf7mxe.autowall.storage.LocalPlaylistStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PreviousWallpaperTileService : TileService() {
    override fun onClick() {
        super.onClick()
        AndroidContext.appContext = applicationContext
        val playlistId = LocalPlaylistStore.activePlaylistId.value ?: return
        CoroutineScope(Dispatchers.IO).launch {
            WallpaperActionRunner.executeAction(playlistId, PreviousInPlaylistAction)
        }
    }
}
