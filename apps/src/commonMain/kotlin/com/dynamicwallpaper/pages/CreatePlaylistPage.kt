package com.dynamicwallpaper.pages

import com.dynamicwallpaper.Playlist
import com.dynamicwallpaper.Rule
import com.dynamicwallpaper.SubPlaylist
import com.dynamicwallpaper.storage.LocalPlaylistStore
import com.dynamicwallpaper.storage.saveImage
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
import kotlin.uuid.Uuid

data class PickedImage(
    val imageId: String,
    val file: FileReference,
    val source: ImageSource,
)

@Routable("/playlists/create")
class CreatePlaylistPage : Page {

    // Generate ID upfront so we can save temp playlist for rule/sub-playlist creation
    val playlistId = Uuid.random()

    val playlistName = Signal("")
    val pickedImages = Signal<List<PickedImage>>(emptyList())
    val rules = Signal<List<Rule>>(emptyList())
    val subPlaylists = Signal<List<SubPlaylist>>(emptyList())

    override fun ViewWriter.render() {
        scrolling.col {
            h2 { content = "Create Playlist" }

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
                    subtext { ::content { "${pickedImages().size} selected" } }
                }
                space()

                // Image grid
                col {
                    ::shown { pickedImages().isNotEmpty() }
                    forEach(pickedImages) { img ->
                        row {
                            sizeConstraints(width = 4.rem, height = 4.rem).image {
                                source = img.source
                                scaleType = ImageScaleType.Crop
                            }
                            expanding.text { content = img.imageId.take(8) }
                            danger.button {
                                icon(Icon.close, "Remove")
                                onClick {
                                    pickedImages.value = pickedImages.value.filter { it.imageId != img.imageId }
                                }
                            }
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
                            PickedImage(
                                imageId = Uuid.random().toString(),
                                file = file,
                                source = ImageLocal(file),
                            )
                        }
                        pickedImages.value = pickedImages.value + newImages
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
                        // Save temp playlist so the rule wizard can add rules to it
                        saveTempPlaylist()
                        pageNavigator.navigate(SelectTriggerPage(playlistId.toString()))
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
                        saveTempPlaylist()
                        pageNavigator.navigate(EditSubPlaylistPage(playlistId.toString(), "-1"))
                    }
                }
            }

            space()

            // Action buttons
            row {
                expanding.button {
                    text { content = "Cancel" }
                    onClick {
                        // Clean up temp playlist if it was saved
                        LocalPlaylistStore.getById(playlistId)?.let {
                            LocalPlaylistStore.delete(playlistId)
                        }
                        pageNavigator.goBack()
                    }
                }
                expanding.important.button {
                    text { content = "Save Playlist" }
                    action = Action("Save") {
                        val name = playlistName.await()
                        if (name.isBlank()) {
                            throw PlainTextException("Please enter a playlist name.", "Validation Error")
                        }
                        val images = pickedImages.await()

                        // Save each picked image to persistent storage
                        for (img in images) {
                            saveImage(playlistId.toString(), img.imageId, img.file)
                        }

                        val playlist = Playlist(
                            _id = playlistId,
                            name = name,
                            photoFileNames = images.map { it.imageId },
                            rules = rules.await(),
                            subPlaylists = subPlaylists.await(),
                        )
                        LocalPlaylistStore.save(playlist)
                        pageNavigator.goBack()
                    }
                }
            }
        }
    }

    private fun saveTempPlaylist() {
        val playlist = Playlist(
            _id = playlistId,
            name = playlistName.value.ifBlank { "Untitled" },
            photoFileNames = pickedImages.value.map { it.imageId },
            rules = rules.value,
            subPlaylists = subPlaylists.value,
        )
        LocalPlaylistStore.save(playlist)
    }
}
