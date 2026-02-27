package com.dynamicwallpaper.engine

import com.dynamicwallpaper.*
import kotlin.random.Random

object PlaylistEngine {

    fun goToNext(playlist: Playlist): Playlist {
        if (playlist.selectedSubPlaylistIndex >= 0) {
            val sub = playlist.subPlaylists.getOrNull(playlist.selectedSubPlaylistIndex)
                ?: return playlist
            if (sub.fileNames.isEmpty()) return playlist
            val nextIndex = (playlist.subPlaylistSelectedImageIndex + 1) % sub.fileNames.size
            return playlist.copy(subPlaylistSelectedImageIndex = nextIndex)
        }
        if (playlist.photoFileNames.isEmpty()) return playlist
        val nextIndex = (playlist.selectedImageIndex + 1) % playlist.photoFileNames.size
        return playlist.copy(selectedImageIndex = nextIndex)
    }

    fun goToPrevious(playlist: Playlist): Playlist {
        if (playlist.selectedSubPlaylistIndex >= 0) {
            val sub = playlist.subPlaylists.getOrNull(playlist.selectedSubPlaylistIndex)
                ?: return playlist
            if (sub.fileNames.isEmpty()) return playlist
            val prevIndex = if (playlist.subPlaylistSelectedImageIndex <= 0)
                sub.fileNames.size - 1 else playlist.subPlaylistSelectedImageIndex - 1
            return playlist.copy(subPlaylistSelectedImageIndex = prevIndex)
        }
        if (playlist.photoFileNames.isEmpty()) return playlist
        val prevIndex = if (playlist.selectedImageIndex <= 0)
            playlist.photoFileNames.size - 1 else playlist.selectedImageIndex - 1
        return playlist.copy(selectedImageIndex = prevIndex)
    }

    fun goToRandom(playlist: Playlist): Playlist {
        if (playlist.selectedSubPlaylistIndex >= 0) {
            val sub = playlist.subPlaylists.getOrNull(playlist.selectedSubPlaylistIndex)
                ?: return playlist
            if (sub.fileNames.size <= 1) return playlist
            var idx = Random.nextInt(sub.fileNames.size)
            if (idx == playlist.subPlaylistSelectedImageIndex)
                idx = (idx + 1) % sub.fileNames.size
            return playlist.copy(subPlaylistSelectedImageIndex = idx)
        }
        if (playlist.photoFileNames.size <= 1) return playlist
        var idx = Random.nextInt(playlist.photoFileNames.size)
        if (idx == playlist.selectedImageIndex)
            idx = (idx + 1) % playlist.photoFileNames.size
        return playlist.copy(selectedImageIndex = idx)
    }

    fun goToSpecific(playlist: Playlist, fileName: String): Playlist {
        // Check sub-playlists first
        if (playlist.selectedSubPlaylistIndex >= 0) {
            val sub = playlist.subPlaylists.getOrNull(playlist.selectedSubPlaylistIndex)
            if (sub != null) {
                val idx = sub.fileNames.indexOf(fileName)
                if (idx >= 0) return playlist.copy(subPlaylistSelectedImageIndex = idx)
            }
        }
        // Check top-level
        val idx = playlist.photoFileNames.indexOf(fileName)
        if (idx >= 0) return playlist.copy(
            selectedImageIndex = idx,
            selectedSubPlaylistIndex = -1
        )
        return playlist
    }

    fun switchToSubPlaylist(playlist: Playlist, name: String): Playlist {
        val idx = playlist.subPlaylists.indexOfFirst { it.name == name }
        if (idx < 0) return playlist
        return playlist.copy(
            selectedSubPlaylistIndex = idx,
            subPlaylistSelectedImageIndex = 0
        )
    }

    fun getCurrentImageId(playlist: Playlist): String? {
        if (playlist.selectedSubPlaylistIndex >= 0) {
            val sub = playlist.subPlaylists.getOrNull(playlist.selectedSubPlaylistIndex)
                ?: return null
            return sub.fileNames.getOrNull(playlist.subPlaylistSelectedImageIndex)
        }
        return playlist.photoFileNames.getOrNull(playlist.selectedImageIndex)
    }

    fun executeAction(playlist: Playlist, action: Action): Playlist = when (action) {
        is NextInPlaylist -> goToNext(playlist)
        is PreviousInPlaylist -> goToPrevious(playlist)
        is RandomInPlaylist -> goToRandom(playlist)
        is SwitchToSubPlaylist -> switchToSubPlaylist(playlist, action.subPlaylistName)
        is SpecificWallpaper -> goToSpecific(playlist, action.imageFileName)
    }
}
