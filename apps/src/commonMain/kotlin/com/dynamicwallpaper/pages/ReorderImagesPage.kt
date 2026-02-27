package com.dynamicwallpaper.pages

import com.dynamicwallpaper.storage.LocalPlaylistStore
import com.dynamicwallpaper.storage.loadImageSource
import com.lightningkite.kiteui.Routable
import com.lightningkite.kiteui.models.*
import com.lightningkite.kiteui.navigation.Page
import com.lightningkite.kiteui.navigation.pageNavigator
import com.lightningkite.kiteui.reactive.*
import com.lightningkite.kiteui.views.*
import com.lightningkite.kiteui.views.direct.*
import com.lightningkite.kiteui.views.l2.*
import com.lightningkite.reactive.core.Signal
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

data class ReorderItem(
    val imageId: String,
    val source: ImageSource?,
)

@Routable("/playlists/{playlistId}/reorder")
class ReorderImagesPage(val playlistId: String) : Page {

    val playlist = LocalPlaylistStore.getById(Uuid.parse(playlistId))
    val items = Signal<List<ReorderItem>>(emptyList())
    val loaded = Signal(false)

    private fun loadImages() {
        if (playlist == null || loaded.value) return
        GlobalScope.launch {
            val loadedItems = playlist.photoFileNames.map { imageId ->
                ReorderItem(
                    imageId = imageId,
                    source = loadImageSource(playlistId, imageId),
                )
            }
            items.value = loadedItems
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
            row {
                expanding.h2 { content = "Reorder Images" }
            }
            subtext { content = "Use the arrows to move images up or down in the playlist order." }

            space()

            forEach(items) { item ->
                card.row {
                    if (item.source != null) {
                        sizeConstraints(width = 4.rem, height = 4.rem).image {
                            source = item.source
                            scaleType = ImageScaleType.Crop
                        }
                    } else {
                        sizeConstraints(width = 4.rem, height = 4.rem).centered.text {
                            content = "?"
                        }
                    }
                    expanding.text { content = item.imageId.take(8) }
                    button {
                        text { content = "Up" }
                        onClick {
                            val list = items.value.toMutableList()
                            val idx = list.indexOfFirst { it.imageId == item.imageId }
                            if (idx > 0) {
                                val temp = list[idx - 1]
                                list[idx - 1] = list[idx]
                                list[idx] = temp
                                items.value = list
                            }
                        }
                    }
                    button {
                        text { content = "Down" }
                        onClick {
                            val list = items.value.toMutableList()
                            val idx = list.indexOfFirst { it.imageId == item.imageId }
                            if (idx >= 0 && idx < list.size - 1) {
                                val temp = list[idx + 1]
                                list[idx + 1] = list[idx]
                                list[idx] = temp
                                items.value = list
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
                    text { content = "Save Order" }
                    onClick {
                        val newOrder = items.value.map { it.imageId }
                        val updated = playlist.copy(photoFileNames = newOrder)
                        LocalPlaylistStore.save(updated)
                        pageNavigator.goBack()
                    }
                }
            }
        }
    }
}
