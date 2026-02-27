package com.dynamicwallpaper.pages

import com.dynamicwallpaper.SubPlaylist
import com.dynamicwallpaper.storage.LocalPlaylistStore
import com.dynamicwallpaper.storage.loadImageSource
import com.lightningkite.kiteui.Routable
import com.lightningkite.kiteui.exceptions.PlainTextException
import com.lightningkite.kiteui.models.*
import com.lightningkite.kiteui.navigation.Page
import com.lightningkite.kiteui.navigation.pageNavigator
import com.lightningkite.kiteui.reactive.*
import com.lightningkite.kiteui.views.*
import com.lightningkite.kiteui.views.direct.*
import com.lightningkite.kiteui.views.l2.*
import com.lightningkite.reactive.context.await
import com.lightningkite.reactive.core.Signal
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

data class SelectableImage(
    val imageId: String,
    val source: ImageSource?,
    val selected: Boolean,
)

@Routable("/playlists/{playlistId}/sub/{subIndex}")
class EditSubPlaylistPage(val playlistId: String, val subIndex: String) : Page {

    val playlist = LocalPlaylistStore.getById(Uuid.parse(playlistId))
    val index = subIndex.toIntOrNull() ?: -1
    val existingSub = if (index >= 0) playlist?.subPlaylists?.getOrNull(index) else null

    val subName = Signal(existingSub?.name ?: "")
    val selectableImages = Signal<List<SelectableImage>>(emptyList())
    val loaded = Signal(false)

    private fun loadImages() {
        if (playlist == null || loaded.value) return
        val selectedFileNames = existingSub?.fileNames?.toSet() ?: emptySet()
        GlobalScope.launch {
            val loadedImages = playlist.photoFileNames.map { imageId ->
                SelectableImage(
                    imageId = imageId,
                    source = loadImageSource(playlistId, imageId),
                    selected = imageId in selectedFileNames,
                )
            }
            selectableImages.value = loadedImages
            loaded.value = true
        }
    }

    override fun ViewWriter.render() {
        if (playlist == null) {
            centered.col {
                h3 { content = "Playlist not found" }
                button {
                    text { content = "Go Back" }
                    onClick { pageNavigator.goBack() }
                }
            }
            return
        }

        // Trigger image loading
        loadImages()

        scrolling.col {
            h2 { content = if (existingSub != null) "Edit Sub-Playlist" else "Create Sub-Playlist" }

            space()

            field("Sub-Playlist Name") {
                fieldTheme.textInput {
                    hint = "Enter a name"
                    keyboardHints = KeyboardHints.title
                    content bind subName
                }
            }

            space()

            h3 { content = "Select Images" }
            subtext { ::content { "${selectableImages().count { it.selected }} of ${selectableImages().size} selected" } }

            space()

            forEach(selectableImages) { img ->
                card.row {
                    if (img.source != null) {
                        sizeConstraints(width = 4.rem, height = 4.rem).image {
                            source = img.source
                            scaleType = ImageScaleType.Crop
                        }
                    } else {
                        sizeConstraints(width = 4.rem, height = 4.rem).centered.text {
                            content = "?"
                        }
                    }
                    expanding.text { content = img.imageId.take(8) }
                    button {
                        icon(
                            if (img.selected) Icon.done else Icon.add,
                            if (img.selected) "Selected" else "Select"
                        )
                        onClick {
                            selectableImages.value = selectableImages.value.map {
                                if (it.imageId == img.imageId) it.copy(selected = !it.selected) else it
                            }
                        }
                    }
                }
            }

            space()

            row {
                expanding.button {
                    text { content = "Cancel" }
                    onClick { pageNavigator.goBack() }
                }
                expanding.important.button {
                    text { content = "Save" }
                    action = Action("Save") {
                        val name = subName.await()
                        if (name.isBlank()) {
                            throw PlainTextException("Please enter a sub-playlist name.", "Validation Error")
                        }
                        val selectedIds = selectableImages.await().filter { it.selected }.map { it.imageId }
                        val sub = SubPlaylist(name = name, fileNames = selectedIds)

                        val subs = playlist.subPlaylists.toMutableList()
                        if (index >= 0 && index < subs.size) {
                            subs[index] = sub
                        } else {
                            subs.add(sub)
                        }
                        val updated = playlist.copy(subPlaylists = subs)
                        LocalPlaylistStore.save(updated)
                        pageNavigator.goBack()
                    }
                }
            }
        }
    }
}
