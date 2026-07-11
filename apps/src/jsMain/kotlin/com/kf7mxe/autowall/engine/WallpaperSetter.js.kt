package com.kf7mxe.autowall.engine

import kotlin.uuid.Uuid

//actual suspend fun setWallpaper(playlistId: String, imageId: String) {
//    // Web platform cannot set device wallpaper - no-op
//}
actual suspend fun setWallpaper(playlistId: String, imageId: Uuid) {
}