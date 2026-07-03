package com.kf7mxe.autowall.storage

import com.kf7mxe.autowall.WallpaperPack
import com.kf7mxe.autowall.Playlist
import com.kf7mxe.autowall.sdk.selectedApi

fun serverFileUrl(fileName: String): String {
    val base = selectedApi.value.http.trimEnd('/')
    return "$base/files/$fileName"
}

fun packPreviewUrl(wallpaperPack: WallpaperPack, index: Int = 0): String? {
    return wallpaperPack.previewImageUrls.getOrNull(index)
        ?: wallpaperPack.imageFileNames.getOrNull(index)?.let { serverFileUrl(it) }
}

fun playlistPreviewUrl(playlist: Playlist, index: Int = 0): String? {
    return playlist.photoFileNames.getOrNull(index)?.let { serverFileUrl(it) }
}
