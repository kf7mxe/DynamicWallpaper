package com.dynamicwallpaper.storage

import com.dynamicwallpaper.Pack
import com.dynamicwallpaper.Playlist
import com.dynamicwallpaper.sdk.selectedApi

fun serverFileUrl(fileName: String): String {
    val base = selectedApi.value.http.trimEnd('/')
    return "$base/files/$fileName"
}

fun packPreviewUrl(pack: Pack, index: Int = 0): String? {
    return pack.previewImageUrls.getOrNull(index)
        ?: pack.imageFileNames.getOrNull(index)?.let { serverFileUrl(it) }
}

fun playlistPreviewUrl(playlist: Playlist, index: Int = 0): String? {
    return playlist.photoFileNames.getOrNull(index)?.let { serverFileUrl(it) }
}
