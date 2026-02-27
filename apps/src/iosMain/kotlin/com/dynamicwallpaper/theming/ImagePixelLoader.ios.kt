package com.dynamicwallpaper.theming

actual suspend fun loadImagePixels(playlistId: String, imageId: String, maxSize: Int): ImagePixels? {
    // iOS doesn't support wallpaper setting, so dynamic theming is not needed
    return null
}
