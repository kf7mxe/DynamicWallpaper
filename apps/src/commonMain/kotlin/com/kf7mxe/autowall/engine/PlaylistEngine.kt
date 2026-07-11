package com.kf7mxe.autowall.engine

import com.foodecision.sdk.session
import com.kf7mxe.autowall.*
import com.lightningkite.reactive.context.invoke
import kotlin.collections.indexOfFirst
import kotlin.random.Random
import kotlin.uuid.Uuid

object PlaylistEngine {

    suspend fun goToNext(playlist: Playlist): Playlist {
        if ((playlist.currentSubPlaylistIndex?:-1) >= 0) {
            val subId = playlist.currentSubPlaylistIndex?.let { playlist.subPlaylists.getOrNull(it)}
                ?: return playlist
            val sub: SubPlaylist = session().subPlaylist.detail(subId)?:return playlist
            if (sub.wallpapers.isEmpty()) return playlist
            val nextIndex = ((playlist.currentSubPlaylistWallpaperIndex?:-1) + 1) % sub.wallpapers.size
            return playlist.copy(currentSubPlaylistWallpaperIndex = nextIndex)
        }
        if (playlist.wallpapers.isEmpty()) return playlist
        val nextIndex = (playlist.currentImageIndex + 1) % playlist.wallpapers.size
        return playlist.copy(currentImageIndex = nextIndex)
    }

   suspend fun goToPrevious(playlist: Playlist): Playlist {
        if ((playlist.currentSubPlaylistIndex?:-1) >= 0) {
            val subId = playlist. currentSubPlaylistIndex?.let { playlist.subPlaylists.getOrNull(it) }
                ?: return playlist
            val sub = session().subPlaylist.detail(subId)?:return playlist
            if (sub.wallpapers.isEmpty()) return playlist
            val prevIndex = if ((playlist.currentSubPlaylistWallpaperIndex?:-1) <= 0)
                sub.wallpapers.size - 1 else (playlist.currentSubPlaylistWallpaperIndex?:0) - 1
            return playlist.copy(currentSubPlaylistWallpaperIndex = prevIndex)
        }
        if (playlist.wallpapers.isEmpty()) return playlist
        val prevIndex = if (playlist.currentImageIndex <= 0)
            playlist.wallpapers.size - 1 else playlist.currentImageIndex - 1
        return playlist.copy(currentImageIndex = prevIndex)
    }

    suspend fun goToRandom(playlist: Playlist): Playlist {
        if ((playlist.currentSubPlaylistIndex?:-1) >= 0) {
            val subId= playlist.currentSubPlaylistIndex?.let { playlist.subPlaylists.getOrNull(it) }
                ?: return playlist
            val sub = session().subPlaylist.detail(subId)?:return playlist
            if (sub.wallpapers.size <= 1) return playlist
            var idx = Random.nextInt(sub.wallpapers.size)
            if (idx == playlist.currentSubPlaylistWallpaperIndex)
                idx = (idx + 1) % sub.wallpapers.size
            return playlist.copy(currentSubPlaylistWallpaperIndex = idx)
        }
        if (playlist.wallpapers.size <= 1) return playlist
        var idx = Random.nextInt(playlist.wallpapers.size)
        if (idx == playlist.currentImageIndex)
            idx = (idx + 1) % playlist.wallpapers.size
        return playlist.copy(currentImageIndex = idx)
    }

    suspend fun goToSpecific(playlist: Playlist, wallpaperId: Uuid): Playlist {
        // Check sub-playlists first
        if ((playlist.currentSubPlaylistIndex?:-1) >= 0) {
            val subId = playlist.subPlaylists.getOrNull(playlist.currentImageIndex)
            val sub = subId?.let {session().subPlaylist.detail(it)}?: return playlist
                val idx = sub.wallpapers.indexOf(wallpaperId)
                if (idx >= 0) return playlist.copy(currentSubPlaylistWallpaperIndex = idx)

        }
        // Check top-level
        val idx = playlist.wallpapers.indexOf(wallpaperId)
        if (idx >= 0) return playlist.copy(
            currentImageIndex = idx,
            currentSubPlaylistIndex = -1
        )
        return playlist
    }

    fun switchToSubPlaylist(playlist: Playlist, toPlaylistId: Uuid): Playlist {
        val idx = playlist.subPlaylists.indexOfFirst { it == toPlaylistId }
        if (idx < 0) return playlist
        return playlist.copy(
            currentSubPlaylistIndex = idx,
            currentSubPlaylistWallpaperIndex = 0
        )
    }

    suspend fun getCurrentImageId(playlist: Playlist): Uuid? {
        if ((playlist.currentSubPlaylistIndex?:-1) >= 0) {
            val subId = playlist.currentSubPlaylistIndex?.let {playlist.subPlaylists.getOrNull(it) }
                ?: return null
            val sub = session().subPlaylist.detail(subId)
            return playlist.currentSubPlaylistWallpaperIndex?.let {sub?.wallpapers?.getOrNull(it) }
        }
        return playlist.wallpapers.getOrNull(playlist.currentImageIndex)
    }

    suspend fun executeAction(playlist: Playlist, action: PlaylistAction): Playlist = when (action) {
        is NextInPlaylistAction -> goToNext(playlist)
        is PreviousInPlaylistAction -> goToPrevious(playlist)
        is RandomInPlaylistAction -> goToRandom(playlist)
        is SwitchToSubPlaylistAction -> switchToSubPlaylist(playlist, action.subPlaylistUuid)
        is SpecificWallpaperAction -> goToSpecific(playlist, action.wallpaperId)
    }
}
