package com.dynamicwallpaper.theming

data class ImagePixels(val pixels: FloatArray, val width: Int, val height: Int)

expect suspend fun loadImagePixels(playlistId: String, imageId: String, maxSize: Int = 64): ImagePixels?
