package com.dynamicwallpaper.storage

import com.dynamicwallpaper.Playlist
import com.lightningkite.reactive.core.Signal
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid

private const val PLAYLISTS_FILE = "playlists.json"
private const val ACTIVE_PLAYLIST_FILE = "active_playlist.json"

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    prettyPrint = false
}

object LocalPlaylistStore {

    val playlists: Signal<List<Playlist>> = Signal(load())

    val activePlaylistId: Signal<Uuid?> = Signal(loadActivePlaylistId())

    fun save(playlist: Playlist) {
        val current = playlists.value.toMutableList()
        val index = current.indexOfFirst { it._id == playlist._id }
        if (index >= 0) {
            current[index] = playlist
        } else {
            current.add(playlist)
        }
        playlists.value = current
        persist()
    }

    fun delete(playlistId: Uuid) {
        deletePlaylistImages(playlistId.toString())
        playlists.value = playlists.value.filter { it._id != playlistId }
        if (activePlaylistId.value == playlistId) {
            setActivePlaylist(null)
        }
        persist()
    }

    fun getById(playlistId: Uuid): Playlist? {
        return playlists.value.find { it._id == playlistId }
    }

    fun setActivePlaylist(playlistId: Uuid?) {
        activePlaylistId.value = playlistId
        if (playlistId != null) {
            writeLocalFile(ACTIVE_PLAYLIST_FILE, playlistId.toString())
        } else {
            writeLocalFile(ACTIVE_PLAYLIST_FILE, "")
        }
    }

    private fun load(): List<Playlist> {
        val raw = readLocalFile(PLAYLISTS_FILE) ?: return emptyList()
        return try {
            json.decodeFromString(ListSerializer(Playlist.serializer()), raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun persist() {
        val raw = json.encodeToString(ListSerializer(Playlist.serializer()), playlists.value)
        writeLocalFile(PLAYLISTS_FILE, raw)
    }

    private fun loadActivePlaylistId(): Uuid? {
        val raw = readLocalFile(ACTIVE_PLAYLIST_FILE)?.trim() ?: return null
        return if (raw.isNotEmpty()) {
            try { Uuid.parse(raw) } catch (e: Exception) { null }
        } else null
    }
}
