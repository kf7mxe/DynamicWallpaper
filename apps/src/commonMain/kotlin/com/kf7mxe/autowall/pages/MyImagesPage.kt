package com.kf7mxe.autowall.pages

import com.kf7mxe.autowall.storage.LocalPlaylistStore
import com.kf7mxe.autowall.storage.loadImageSource
import com.kf7mxe.autowall.storage.saveImage
import com.lightningkite.kiteui.Routable
import com.lightningkite.kiteui.models.*
import com.lightningkite.kiteui.navigation.Page
import com.lightningkite.kiteui.reactive.*
import com.lightningkite.kiteui.requestFiles
import com.lightningkite.kiteui.views.*
import com.lightningkite.kiteui.views.direct.*
import com.lightningkite.kiteui.views.l2.*
import com.lightningkite.reactive.context.invoke
import com.lightningkite.reactive.core.Signal
import com.lightningkite.reactive.core.rememberSuspending
import kotlin.uuid.Uuid

@Routable("/my-images")
class MyImagesPage : Page {
    override fun ElementWriter.CanAddTheme.render() {
        val playlists = LocalPlaylistStore.playlists

        val allImages = rememberSuspending {
//            val result = mutableListOf<PlaylistImage>()
//            for (playlist in playlists()) {
//                for (imageId in playlist.photoFileNames) {
//                    val source = loadImageSource(playlist._id.toString(), imageId)
//                    if (source != null) {
//                        result.add(PlaylistImage(playlist.name, playlist._id.toString(), imageId, source))
//                    }
//                }
//            }
//            result.toList()
        }

        val isLoading = Signal(true)
        val groupedImages = rememberSuspending {
//            val images = allImages()
//            isLoading.value = false
//            images.groupBy { it.playlistName }
        }

        scrolling.col {
            row {
                expanding.h2 { content = "My Images" }
                important.button {
                    row {
                        icon(Icon.add, "Import")
                        text { content = "Import" }
                    }
                    onClick {
                        val currentPlaylists = playlists.value
                        if (currentPlaylists.isEmpty()) {
                            toast("Create a playlist first to import images")
                            return@onClick
                        }
                        val files = context.requestFiles(listOf("image/*"))
                        if (files.isEmpty()) return@onClick
                        // Add to the first playlist
                        val target = currentPlaylists.first()
                        val newIds = mutableListOf<String>()
                        for (file in files) {
                            val imageId = Uuid.random().toString()
                            saveImage(target._id.toString(), imageId, file)
                            newIds.add(imageId)
                        }
//                        LocalPlaylistStore.save(target.copy(
//                            photoFileNames = target.photoFileNames + newIds
//                        ))
                        toast("Added ${files.size} images to '${target.name.ifBlank { "Untitled" }}'")
                    }
                }
            }

            subtext { content = "All images across your playlists" }

            space()

            // Loading
            centered.shownWhen { isLoading() }.activityIndicator { }

            // Empty state
//            centered.shownWhen { allImages().isEmpty() && !isLoading() }.col {
//                icon(Icon.download.copy(width = 4.rem, height = 4.rem), "No images")
//                space()
//                h3 { content = "No Images Yet" }
//                subtext { content = "Import images from the gallery or download packs from the Explore tab." }
//            }

            // Grouped image grid
//            shownWhen { allImages().isNotEmpty() && !isLoading() }.col {
//                forEach(rememberSuspending { groupedImages().keys.toList() }) { playlistName ->
//                    h3 { content = playlistName.ifBlank { "Untitled" } }
//                    subtext { ::content {
//                        val count = groupedImages()[playlistName]?.size ?: 0
//                        "$count images"
//                    } }
//                    col {
//                        forEach(rememberSuspending { groupedImages()[playlistName] ?: emptyList() }) { img ->
//                            sizeConstraints(height = 10.rem).button {
//                                image {
//                                    source = img.source
//                                    scaleType = ImageScaleType.Crop
//                                }
//                                onClick {
//                                    // Show full-screen preview using a dialog-style approach
//                                    // since ImagePreviewPage expects a URL, not a local source
//                                    toast("Image: ${img.imageId}")
//                                }
//                            }
//                        }
//                    }
//                    space()
//                }
//            }
        }
    }
}

private data class PlaylistImage(
    val playlistName: String,
    val playlistId: String,
    val imageId: String,
    val source: ImageSource,
)
