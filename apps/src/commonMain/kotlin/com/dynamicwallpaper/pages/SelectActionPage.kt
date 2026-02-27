package com.dynamicwallpaper.pages

import com.dynamicwallpaper.*
import com.dynamicwallpaper.storage.LocalPlaylistStore
import com.lightningkite.kiteui.Routable
import com.lightningkite.kiteui.exceptions.PlainTextException
import com.lightningkite.kiteui.models.*
import com.lightningkite.kiteui.navigation.Page
import com.lightningkite.kiteui.navigation.pageNavigator
import com.lightningkite.kiteui.reactive.*
import com.lightningkite.kiteui.views.*
import com.lightningkite.kiteui.views.direct.*
import com.lightningkite.kiteui.views.l2.*
import com.lightningkite.reactive.core.Signal
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid

@Routable("/playlists/{playlistId}/rules/action/{encodedTrigger}")
class SelectActionPage(val playlistId: String, val encodedTrigger: String) : Page {

    private enum class ActionType {
        Next, Previous, Random, SwitchSubPlaylist, SpecificWallpaper
    }

    val trigger: Trigger? = try {
        Json.decodeFromString<Trigger>(encodedTrigger)
    } catch (e: Exception) {
        null
    }

    private val selectedAction = Signal<ActionType?>(null)
    val subPlaylistName = Signal("")
    val imageFileName = Signal("")

    override fun ViewWriter.render() {
        if (trigger == null) {
            centered.col {
                h3 { content = "Invalid trigger data" }
                button {
                    text { content = "Go Back" }
                    onClick { pageNavigator.goBack() }
                }
            }
            return
        }

        scrolling.col {
            h2 { content = "Select Action" }

            // Show trigger summary
            card.col {
                subtext { content = "Trigger" }
                text { content = trigger.displayName }
                subtext { content = trigger.displayDescription }
            }

            space()

            subtext { content = "What should happen when this trigger fires?" }

            space()

            // Action options
            actionButton("Next Wallpaper", "Go to the next wallpaper in the playlist", ActionType.Next)
            space()
            actionButton("Previous Wallpaper", "Go to the previous wallpaper in the playlist", ActionType.Previous)
            space()
            actionButton("Random Wallpaper", "Pick a random wallpaper from the playlist", ActionType.Random)
            space()
            actionButton("Switch Sub-Playlist", "Switch to a specific sub-playlist", ActionType.SwitchSubPlaylist)

            // Sub-playlist name input
            col {
                ::shown { selectedAction() == ActionType.SwitchSubPlaylist }
                space()
                field("Sub-Playlist Name") {
                    fieldTheme.textInput {
                        hint = "Enter sub-playlist name"
                        content bind subPlaylistName
                    }
                }
            }

            space()
            actionButton("Specific Wallpaper", "Set a specific wallpaper by filename", ActionType.SpecificWallpaper)

            // Image filename input
            col {
                ::shown { selectedAction() == ActionType.SpecificWallpaper }
                space()
                field("Image Filename") {
                    fieldTheme.textInput {
                        hint = "Enter image ID"
                        content bind imageFileName
                    }
                }
            }

            space()
            space()

            // Save button
            row {
                expanding.button {
                    text { content = "Cancel" }
                    onClick { pageNavigator.goBack() }
                }
                expanding.important.button {
                    text { content = "Save Rule" }
                    action = Action("Save Rule") {
                        val actionType = selectedAction.value
                            ?: throw PlainTextException("Please select an action.", "Validation Error")

                        val ruleAction: com.dynamicwallpaper.Action = when (actionType) {
                            ActionType.Next -> NextInPlaylist
                            ActionType.Previous -> PreviousInPlaylist
                            ActionType.Random -> RandomInPlaylist
                            ActionType.SwitchSubPlaylist -> {
                                val name = subPlaylistName.value.trim()
                                if (name.isBlank()) throw PlainTextException("Please enter a sub-playlist name.", "Validation Error")
                                SwitchToSubPlaylist(name)
                            }
                            ActionType.SpecificWallpaper -> {
                                val fileName = imageFileName.value.trim()
                                if (fileName.isBlank()) throw PlainTextException("Please enter an image filename.", "Validation Error")
                                SpecificWallpaper(fileName)
                            }
                        }

                        val rule = Rule(trigger = trigger, action = ruleAction)

                        // Save rule to playlist in store
                        val playlist = LocalPlaylistStore.getById(Uuid.parse(playlistId))
                        if (playlist != null) {
                            val updated = playlist.copy(rules = playlist.rules + rule)
                            LocalPlaylistStore.save(updated)
                        }

                        // Navigate back to edit playlist (skip trigger config + trigger select pages)
                        pageNavigator.navigate(EditPlaylistPage(playlistId))
                    }
                }
            }
        }
    }

    private fun ViewWriter.actionButton(name: String, description: String, type: ActionType) {
        card.button {
            row {
                expanding.col {
                    text { content = name }
                    subtext { content = description }
                }
                col {
                    ::shown { selectedAction() == type }
                    icon(Icon.done, "Selected")
                }
            }
            onClick { selectedAction.value = type }
        }
    }
}
