package com.kf7mxe.autowall.skills

import com.foodecision.sdk.session
import com.kf7mxe.autowall.Rule
import com.kf7mxe.autowall.SubPlaylist
import com.kf7mxe.autowall.pages.CreatePlaylistPage
import com.kf7mxe.autowall.pages.EditSubPlaylistPage
import com.kf7mxe.autowall.pages.PickedImage
import com.kf7mxe.autowall.pages.SelectTriggerPage
import com.kf7mxe.autowall.selectedPlaylist
import com.kf7mxe.autowall.storage.LocalPlaylistStore
import com.kf7mxe.autowall.storage.saveImage
import com.lightningkite.kiteui.exceptions.PlainTextException
import com.lightningkite.kiteui.models.Icon
import com.lightningkite.kiteui.models.ImageLocal
import com.lightningkite.kiteui.models.ImageScaleType
import com.lightningkite.kiteui.models.KeyboardHints
import com.lightningkite.kiteui.models.rem
import com.lightningkite.kiteui.navigation.mainPageNavigator
import com.lightningkite.kiteui.navigation.pageNavigator
import com.lightningkite.kiteui.reactive.Action
import com.lightningkite.kiteui.requestFiles
import com.lightningkite.kiteui.views.ElementWriter
import com.lightningkite.kiteui.views.card
import com.lightningkite.kiteui.views.danger
import com.lightningkite.kiteui.views.direct.button
import com.lightningkite.kiteui.views.direct.col
import com.lightningkite.kiteui.views.direct.h1
import com.lightningkite.kiteui.views.direct.h2
import com.lightningkite.kiteui.views.direct.h3
import com.lightningkite.kiteui.views.direct.image
import com.lightningkite.kiteui.views.direct.onClick
import com.lightningkite.kiteui.views.direct.row
import com.lightningkite.kiteui.views.direct.scrolling
import com.lightningkite.kiteui.views.direct.sizeConstraints
import com.lightningkite.kiteui.views.direct.space
import com.lightningkite.kiteui.views.direct.subtext
import com.lightningkite.kiteui.views.direct.text
import com.lightningkite.kiteui.views.direct.textInput
import com.lightningkite.kiteui.views.expanding
import com.lightningkite.kiteui.views.fieldTheme
import com.lightningkite.kiteui.views.forEach
import com.lightningkite.kiteui.views.important
import com.lightningkite.kiteui.views.l2.field
import com.lightningkite.kiteui.views.l2.icon
import com.lightningkite.reactive.context.await
import com.lightningkite.reactive.context.reactive
import com.lightningkite.reactive.core.Signal
import kotlin.uuid.Uuid

class CreatePlaylist : Tool {
    override val name = "CreatePlaylist"
    // collapsesInHistory defaults to false (Lightweight)
    override val awaitingInput = Signal(true)

    override fun matches(query: String) = query == ""



    // Generate ID upfront so we can save temp playlist for rule/sub-playlist creation
    val playlistId = Uuid.random()

    val playlistName = Signal("")
    val pickedImages = Signal<List<PickedImage>>(emptyList())
    val rules = Signal<List<Rule>>(emptyList())
    val subPlaylists = Signal<List<SubPlaylist>>(emptyList())




    override fun ElementWriter.CanAddTheme.render(query: String?,params: Map<String, Any>?) {
         scrolling.col {
            h2 { content = "Create Playlist" }
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
//                        saveTempPlaylist()
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
                            subtext { content = "${sub.wallpapers.size} images" }
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
//                        saveTempPlaylist()
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

//                        val playlist = Playlist(
//                            _id = playlistId,
//                            name = name,
//                            photoFileNames = images.map { it.imageId },
//                            rules = rules.await(),
//                            subPlaylists = subPlaylists.await(),
//                        )
//                        LocalPlaylistStore.save(playlist)
                        showToast("Playlist Created")
                        goToNextTool(SuccessTool(), null)
                    }
                }
            }
        }


    }
//        }
}