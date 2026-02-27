package com.dynamicwallpaper.storage

import com.dynamicwallpaper.Pack
import com.dynamicwallpaper.Playlist
import com.lightningkite.kiteui.fetch
import kotlin.uuid.Uuid

object ImageDownloader {

    data class DownloadProgress(
        val currentIndex: Int,
        val totalCount: Int,
        val currentBytes: Long,
        val totalBytes: Long,
    )

    suspend fun downloadPackImages(
        pack: Pack,
        targetPlaylistId: String,
        onProgress: (DownloadProgress) -> Unit = {},
    ): List<String> {
        val imageIds = mutableListOf<String>()
        val total = pack.imageFileNames.size

        pack.imageFileNames.forEachIndexed { index, fileName ->
            val imageId = Uuid.random().toString()
            val url = serverFileUrl(fileName)
            val response = fetch(
                url = url,
                onDownloadProgress = { done, expected ->
                    onProgress(DownloadProgress(index, total, done, expected))
                },
            )
            saveImageFromBlob(targetPlaylistId, imageId, response.blob())
            imageIds.add(imageId)
        }
        return imageIds
    }

    suspend fun downloadPlaylistImages(
        playlist: Playlist,
        targetPlaylistId: String,
        onProgress: (DownloadProgress) -> Unit = {},
    ): List<String> {
        val imageIds = mutableListOf<String>()
        val total = playlist.photoFileNames.size

        playlist.photoFileNames.forEachIndexed { index, fileName ->
            val imageId = Uuid.random().toString()
            val url = serverFileUrl(fileName)
            val response = fetch(
                url = url,
                onDownloadProgress = { done, expected ->
                    onProgress(DownloadProgress(index, total, done, expected))
                },
            )
            saveImageFromBlob(targetPlaylistId, imageId, response.blob())
            imageIds.add(imageId)
        }
        return imageIds
    }
}
