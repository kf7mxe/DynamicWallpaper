package com.dynamicwallpaper.engine

actual suspend fun setWallpaper(playlistId: String, imageId: String) {
    // Web platform cannot set device wallpaper - no-op
}
