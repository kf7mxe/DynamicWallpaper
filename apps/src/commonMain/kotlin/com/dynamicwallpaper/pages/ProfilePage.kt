package com.dynamicwallpaper.pages

import com.dynamicwallpaper.User
import com.dynamicwallpaper.engine.WallpaperActionRunner
import com.dynamicwallpaper.engine.WallpaperTarget
import com.dynamicwallpaper.engine.WallpaperTargetManager
import com.dynamicwallpaper.engine.scheduleTriggers
import com.dynamicwallpaper.name
import com.dynamicwallpaper.sdk.*
import com.dynamicwallpaper.storage.LocalPlaylistStore
import com.dynamicwallpaper.storage.deletePlaylistImages
import com.dynamicwallpaper.theming.DynamicThemeManager
import com.dynamicwallpaper.theming.ThemeMode
import com.dynamicwallpaper.theming.ThemeModeManager
import com.lightningkite.kiteui.Routable
import com.lightningkite.kiteui.models.*
import com.lightningkite.kiteui.navigation.Page
import com.lightningkite.kiteui.navigation.pageNavigator
import com.lightningkite.kiteui.reactive.*
import com.lightningkite.reactive.context.invoke
import com.lightningkite.kiteui.views.*
import com.lightningkite.kiteui.views.direct.*
import com.lightningkite.kiteui.views.l2.*
import com.lightningkite.reactive.core.Signal
import com.lightningkite.reactive.core.rememberSuspending
import com.lightningkite.services.database.modification

@Routable("/profile")
class ProfilePage : Page {
    override fun ViewWriter.render() {

        val userData = rememberSuspending {
            val s = rawSession() ?: return@rememberSuspending null
            try {
                s.api.user.detail(s.userId)
            } catch (e: Exception) {
                null
            }
        }

        val editingName = Signal(false)
        val nameInput = Signal("")

        scrolling.col {
            h2 { content = "Profile & Settings" }

            space()

            // ── Account Section ──
            card.col {
                h3 { content = "Account" }

                // Signed in state
                shownWhen { sessionToken() != null }.col {
                    // Email
                    row {
                        expanding.text { ::content { userData()?.email?.toString() ?: "Loading..." } }
                    }

                    space()

                    // Name display (not editing)
                    shownWhen { !editingName() }.row {
                        expanding.col {
                            subtext { content = "Name" }
                            text { ::content { userData()?.name?.ifBlank { "No name set" } ?: "..." } }
                        }
                        button {
                            text { content = "Edit" }
                            onClick {
                                nameInput.value = userData()?.name ?: ""
                                editingName.value = true
                            }
                        }
                    }

                    // Name editing
                    shownWhen { editingName() }.col {
                        subtext { content = "Name" }
                        row {
                            expanding.fieldTheme.textInput {
                                hint = "Your name"
                                content bind nameInput
                            }
                            button {
                                text { content = "Save" }
                                action = Action("Save") {
                                    val s = rawSession() ?: return@Action
                                    s.api.user.modify(s.userId, modification<User> {
                                        it.name assign nameInput()
                                    })
                                    editingName.value = false
                                    toast("Name updated")
                                }
                            }
                            button {
                                text { content = "Cancel" }
                                onClick { editingName.value = false }
                            }
                        }
                    }

                    space()

                    danger.button {
                        text { content = "Sign Out" }
                        onClick { logout() }
                    }
                }

                // Signed out state
                shownWhen { sessionToken() == null }.col {
                    subtext {
                        content = "Create an account to sync your playlists across devices."
                    }
                    space()
                    important.button {
                        text { content = "Create Account" }
                        onClick { pageNavigator.navigate(LoginPage()) }
                    }
                    button {
                        text { content = "Sign In" }
                        onClick { pageNavigator.navigate(LoginPage()) }
                    }
                }
            }

            space()

            // ── Settings Section ──
            card.col {
                h3 { content = "Settings" }

                space()

                // Active playlist selector
                field("Active Playlist") {
                    val playlists = LocalPlaylistStore.playlists
                    val activeId = LocalPlaylistStore.activePlaylistId

                    col {
                        forEach(playlists) { playlist ->
                            button {
                                row {
                                    expanding.text { ::content { playlist.name.ifBlank { "Untitled" } } }
                                    shownWhen { activeId() == playlist._id }.icon(Icon.done, "Active")
                                }
                                onClick {
                                    LocalPlaylistStore.setActivePlaylist(playlist._id)
                                    WallpaperActionRunner.applyCurrentWallpaper(playlist._id)
                                    scheduleTriggers()
                                }
                            }
                        }
                        shownWhen { playlists().isEmpty() }.subtext {
                            content = "No playlists yet. Create one from the Playlists tab."
                        }
                        shownWhen { activeId() != null }.button {
                            text { content = "Clear Selection" }
                            onClick { LocalPlaylistStore.setActivePlaylist(null) }
                        }
                    }
                }

                space()

                // Theme mode
                field("Theme Mode") {
                    row {
                        ThemeMode.entries.forEach { mode ->
                            button {
                                text { content = mode.name }
                                onClick { ThemeModeManager.setMode(mode) }
                            }
                        }
                    }
                    subtext { ::content { "Current: ${ThemeModeManager.mode()}" } }
                }

                space()

                // Wallpaper target
                field("Wallpaper Target") {
                    row {
                        button {
                            text { content = "Home" }
                            onClick { WallpaperTargetManager.setTarget(WallpaperTarget.HomeScreen) }
                        }
                        button {
                            text { content = "Lock" }
                            onClick { WallpaperTargetManager.setTarget(WallpaperTarget.LockScreen) }
                        }
                        button {
                            text { content = "Both" }
                            onClick { WallpaperTargetManager.setTarget(WallpaperTarget.Both) }
                        }
                    }
                    subtext { ::content { "Current: ${WallpaperTargetManager.target()}" } }
                }

                space()

                // Dynamic theming toggle
                row {
                    expanding.col {
                        text { content = "Dynamic Colors" }
                        subtext { content = "Extract colors from your current wallpaper" }
                    }
                    switch {
                        checked bind DynamicThemeManager.isEnabled
                    }
                }
                important.button {
                    text { content = "Apply Dynamic Theme" }
                    onClick {
                        DynamicThemeManager.setEnabled(true)
                        DynamicThemeManager.updateThemeFromCurrentWallpaper()
                    }
                }
                button {
                    text { content = "Reset to Default Theme" }
                    onClick {
                        DynamicThemeManager.setEnabled(false)
                        DynamicThemeManager.resetToDefault()
                    }
                }
            }

            space()

            // ── Data Management Section ──
            card.col {
                h3 { content = "Data" }

                space()

                button {
                    text { content = "Clear Image Cache" }
                    onClick {
                        confirmDanger("Clear Image Cache", "This will delete all downloaded images. Your playlists will remain but images will need to be re-downloaded.") {
                            val playlists = LocalPlaylistStore.playlists.value
                            playlists.forEach { playlist ->
                                deletePlaylistImages(playlist._id.toString())
                            }
                            toast("Image cache cleared")
                        }
                    }
                }

                space()

                danger.button {
                    text { content = "Delete All Playlists" }
                    onClick {
                        confirmDanger("Delete All Playlists", "This will permanently delete all playlists and their images. This cannot be undone.") {
                            val playlists = LocalPlaylistStore.playlists.value.toList()
                            playlists.forEach { playlist ->
                                deletePlaylistImages(playlist._id.toString())
                                LocalPlaylistStore.delete(playlist._id)
                            }
                            LocalPlaylistStore.setActivePlaylist(null)
                            toast("All playlists deleted")
                        }
                    }
                }
            }

            space()

            // ── Advanced Section ──
            card.col {
                h3 { content = "Advanced" }

                space()

                field("Server Backend") {
                    col {
                        ApiOption.entries.forEach { option ->
                            button {
                                row {
                                    expanding.text { content = option.apiName }
                                    shownWhen { selectedApi() == option }.icon(Icon.done, "Selected")
                                }
                                onClick { saveSelectedApi(option) }
                            }
                        }
                    }
                }
            }

            space()

            // ── About Section ──
            card.col {
                h3 { content = "About" }
                row {
                    expanding.text { content = "Dynamic Wallpaper" }
                    subtext { content = "v1.0.0" }
                }
                subtext { content = "Automatic wallpaper changer with rules and triggers." }
            }
        }
    }
}
