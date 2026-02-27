package com.dynamicwallpaper.pages

import com.dynamicwallpaper.Playlist
import com.dynamicwallpaper.Rule
import com.dynamicwallpaper.SubPlaylist
import com.dynamicwallpaper.storage.LocalPlaylistStore
import com.dynamicwallpaper.storage.deletePlaylistImages
import com.dynamicwallpaper.storage.loadImageSource
import com.dynamicwallpaper.storage.saveImage
import com.dynamicwallpaper.storage.deleteImage
import com.lightningkite.kiteui.FileReference
import com.lightningkite.kiteui.Routable
import com.lightningkite.kiteui.exceptions.PlainTextException
import com.lightningkite.kiteui.models.*
import com.lightningkite.kiteui.navigation.Page
import com.lightningkite.kiteui.navigation.pageNavigator
import com.lightningkite.kiteui.reactive.*
import com.lightningkite.kiteui.requestFiles
import com.lightningkite.kiteui.views.*
import com.lightningkite.kiteui.views.direct.*
import com.lightningkite.kiteui.views.l2.*
import com.lightningkite.reactive.context.await
import com.lightningkite.reactive.core.Signal
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

data class EditImage(
    val imageId: String,
    val source: ImageSource?,
    val newFile: FileReference? = null, // non-null if newly picked (needs saving)
)

@Routable("/playlists/edit/{playlistId}")
class EditPlaylistPage(val playlistId: String) : Page {

    val existing = LocalPlaylistStore.getById(Uuid.parse(playlistId))
    val playlistName = Signal(existing?.name ?: "")
    val images = Signal<List<EditImage>>(emptyList())
    val rules = Signal(existing?.rules ?: emptyList())
    val subPlaylists = Signal(existing?.subPlaylists ?: emptyList())
    val imagesLoaded = Signal(false)

    private fun loadImages() {
        if (existing == null || imagesLoaded.value) return
        GlobalScope.launch {
            val loaded = existing.photoFileNames.map { imageId ->
                EditImage(
                    imageId = imageId,
                    source = loadImageSource(playlistId, imageId),
                )
            }
            images.value = loaded
            imagesLoaded.value = true
        }
    }

    override fun ViewWriter.render() {
        if (existing == null) {
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
                expanding.h2 { content = "Edit Playlist" }
                danger.button {
                    icon(Icon.delete, "Delete")
                    onClick {
                        confirmDanger("Delete Playlist", "This will permanently delete the playlist and all its images.") {
                            deletePlaylistImages(playlistId)
                            LocalPlaylistStore.delete(existing._id)
                            pageNavigator.goBack()
                        }
                    }
                }
            }

            space()

            field("Playlist Name") {
                fieldTheme.textInput {
                    hint = "Enter a name for your playlist"
                    keyboardHints = KeyboardHints.title
                    content bind playlistName
                }
            }

            space()

            // Image section
            card.col {
                row {
                    expanding.h3 { content = "Images" }
                    subtext { ::content { "${images().size} images" } }
                }
                space()

                // Image list
                col {
                    ::shown { images().isNotEmpty() }
                    forEach(images) { img ->
                        row {
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
                            danger.button {
                                icon(Icon.close, "Remove")
                                onClick {
                                    // If it was an existing image, delete from storage
                                    if (img.newFile == null) {
                                        deleteImage(playlistId, img.imageId)
                                    }
                                    images.value = images.value.filter { it.imageId != img.imageId }
                                }
                            }
                        }
                    }
                }

                // Reorder link
                col {
                    ::shown { images().size > 1 }
                    button {
                        row {
                            icon(Icon.sort, "Reorder")
                            text { content = "Reorder Images" }
                        }
                        onClick {
                            // Save current state first so reorder page can read it
                            saveCurrentState()
                            pageNavigator.navigate(ReorderImagesPage(playlistId))
                        }
                    }
                }

                button {
                    row {
                        icon(Icon.add, "Add")
                        text { content = "Select Images from Gallery" }
                    }
                    onClick {
                        val files = context.requestFiles(listOf("image/*"))
                        val newImages = files.map { file ->
                            EditImage(
                                imageId = Uuid.random().toString(),
                                source = ImageLocal(file),
                                newFile = file,
                            )
                        }
                        images.value = images.value + newImages
                    }
                }
            }

            space()

            // Rules section
            card.col {
                row {
                    expanding.h3 { content = "Rules" }
                    subtext { ::content { "${rules().size} rules" } }
                }

                forEach(rules) { rule ->
                    row {
                        expanding.col {
                            text { content = "${rule.trigger.displayName} -> ${rule.action.displayName}" }
                            subtext { content = rule.trigger.displayDescription }
                        }
                        danger.button {
                            icon(Icon.close, "Delete")
                            onClick {
                                rules.value = rules.value.filter { it !== rule }
                            }
                        }
                    }
                }

                space()
                button {
                    row {
                        icon(Icon.add, "Add")
                        text { content = "Add Rule" }
                    }
                    onClick {
                        saveCurrentState()
                        pageNavigator.navigate(SelectTriggerPage(playlistId))
                    }
                }
            }

            space()

            // Sub-playlists section
            card.col {
                row {
                    expanding.h3 { content = "Sub-Playlists" }
                    subtext { ::content { "${subPlaylists().size} sub-playlists" } }
                }

                forEach(subPlaylists) { sub ->
                    row {
                        expanding.col {
                            text { content = sub.name }
                            subtext { content = "${sub.fileNames.size} images" }
                        }
                        button {
                            icon(Icon.chevronRight, "Edit")
                            onClick {
                                val idx = subPlaylists.value.indexOf(sub)
                                if (idx >= 0) {
                                    saveCurrentState()
                                    pageNavigator.navigate(EditSubPlaylistPage(playlistId, idx.toString()))
                                }
                            }
                        }
                        danger.button {
                            icon(Icon.close, "Delete")
                            onClick {
                                subPlaylists.value = subPlaylists.value.filter { it !== sub }
                            }
                        }
                    }
                }

                space()
                button {
                    row {
                        icon(Icon.add, "Add")
                        text { content = "Add Sub-Playlist" }
                    }
                    onClick {
                        saveCurrentState()
                        pageNavigator.navigate(EditSubPlaylistPage(playlistId, "-1"))
                    }
                }
            }

            space()

            // Action buttons
            row {
                expanding.button {
                    text { content = "Cancel" }
                    onClick {
                        pageNavigator.goBack()
                    }
                }
                expanding.important.button {
                    text { content = "Save Changes" }
                    action = Action("Save") {
                        val name = playlistName.await()
                        if (name.isBlank()) {
                            throw PlainTextException("Please enter a playlist name.", "Validation Error")
                        }
                        val currentImages = images.await()

                        // Save newly picked images to persistent storage
                        for (img in currentImages) {
                            if (img.newFile != null) {
                                saveImage(playlistId, img.imageId, img.newFile)
                            }
                        }

                        val updated = existing.copy(
                            name = name,
                            photoFileNames = currentImages.map { it.imageId },
                            rules = rules.await(),
                            subPlaylists = subPlaylists.await(),
                        )
                        LocalPlaylistStore.save(updated)
                        pageNavigator.goBack()
                    }
                }
            }
        }
    }

    private fun saveCurrentState() {
        val updated = existing?.copy(
            name = playlistName.value,
            photoFileNames = images.value.map { it.imageId },
            rules = rules.value,
            subPlaylists = subPlaylists.value,
        ) ?: return
        LocalPlaylistStore.save(updated)
    }
}
