package com.dynamicwallpaper.engine

actual suspend fun setWallpaper(playlistId: String, imageId: String) {
    // iOS does not allow programmatic wallpaper setting - no-op
}
