package com.dynamicwallpaper.pages

import com.dynamicwallpaper.Playlist
import com.dynamicwallpaper.engine.PlaylistEngine
import com.dynamicwallpaper.storage.LocalPlaylistStore
import com.dynamicwallpaper.storage.loadImageSource
import com.dynamicwallpaper.storage.readLocalFile
import com.dynamicwallpaper.storage.writeLocalFile
import com.lightningkite.kiteui.Routable
import com.lightningkite.kiteui.models.*
import com.lightningkite.kiteui.navigation.Page
import com.lightningkite.kiteui.navigation.pageNavigator
import com.lightningkite.kiteui.reactive.*
import com.lightningkite.reactive.context.invoke
import com.lightningkite.reactive.core.Signal
import com.lightningkite.reactive.core.rememberSuspending
import com.lightningkite.kiteui.views.*
import com.lightningkite.kiteui.views.direct.*
import com.lightningkite.kiteui.views.l2.*

@Routable("/playlists")
class PlaylistsPage : Page {
    override fun ViewWriter.render() {
        col {
            row {
                expanding.h2 { content = "My Playlists" }
                important.button {
                    row {
                        icon(Icon.add, "Create")
                        text { content = "New" }
                    }
                    onClick {
                        pageNavigator.navigate(CreatePlaylistPage())
                    }
                }
            }

            space()

            val playlists = LocalPlaylistStore.playlists
            val activeId = LocalPlaylistStore.activePlaylistId

            // Onboarding hint (shown once)
            val onboardingDismissed = Signal(readLocalFile("onboarding_shown") == "true")
            shownWhen { !onboardingDismissed() && playlists().isEmpty() }.card.col {
                h3 { content = "Welcome to Dynamic Wallpaper" }
                subtext { content = "Create playlists with rules to automatically change your wallpaper based on time, weather, location, and more." }
                space()
                button {
                    text { content = "Got it" }
                    onClick {
                        writeLocalFile("onboarding_shown", "true")
                        onboardingDismissed.value = true
                    }
                }
            }

            // Active playlist dashboard
            shownWhen { activeId() != null }.col {
                val activePlaylist = rememberSuspending {
                    val id = activeId() ?: return@rememberSuspending null
                    playlists().find { it._id == id }
                }

                shownWhen { activePlaylist() != null }.card.col {
                    subtext { content = "NOW PLAYING" }
                    h3 { ::content { activePlaylist()?.name ?: "" } }

                    // Current image info
                    row {
                        val currentImageId = rememberSuspending {
                            activePlaylist()?.let { PlaylistEngine.getCurrentImageId(it) }
                        }
                        val totalImages = rememberSuspending {
                            val p = activePlaylist() ?: return@rememberSuspending 0
                            if (p.selectedSubPlaylistIndex >= 0) {
                                p.subPlaylists.getOrNull(p.selectedSubPlaylistIndex)?.fileNames?.size ?: 0
                            } else {
                                p.photoFileNames.size
                            }
                        }
                        val currentIndex = rememberSuspending {
                            val p = activePlaylist() ?: return@rememberSuspending 0
                            if (p.selectedSubPlaylistIndex >= 0) p.subPlaylistSelectedImageIndex + 1
                            else p.selectedImageIndex + 1
                        }
                        subtext { ::content { "Image ${currentIndex()} of ${totalImages()}" } }
                    }

                    // Rules summary
                    shownWhen { (activePlaylist()?.rules?.size ?: 0) > 0 }.row {
                        subtext { ::content {
                            val rules = activePlaylist()?.rules ?: emptyList()
                            val triggerTypes = rules.map { it.trigger.displayName }.distinct()
                            "${rules.size} rules: ${triggerTypes.joinToString(", ")}"
                        } }
                    }

                    space()

                    // Quick action buttons
                    row {
                        button {
                            icon(Icon.arrowBack, "Previous")
                            onClick {
                                val p = activePlaylist() ?: return@onClick
                                LocalPlaylistStore.save(PlaylistEngine.goToPrevious(p))
                            }
                        }
                        button {
                            icon(Icon.sync, "Random")
                            onClick {
                                val p = activePlaylist() ?: return@onClick
                                LocalPlaylistStore.save(PlaylistEngine.goToRandom(p))
                            }
                        }
                        button {
                            icon(Icon.chevronRight, "Next")
                            onClick {
                                val p = activePlaylist() ?: return@onClick
                                LocalPlaylistStore.save(PlaylistEngine.goToNext(p))
                            }
                        }
                    }
                }
            }

            space()

            // Empty state
            shownWhen { playlists().isEmpty() }.centered.col {
                icon(Icon.list.copy(width = 4.rem, height = 4.rem), "No playlists")
                space()
                h3 { content = "No Playlists Yet" }
                subtext { content = "Tap the button above to create your first wallpaper playlist." }
            }

            // Playlist list
            expanding.recyclerView {
                children(playlists, id = { it._id }) { playlist ->
                    link {
                        ::to { { EditPlaylistPage(playlist()._id.toString()) } }
                        card.col {
                            row {
                                expanding.col {
                                    h3 { ::content { playlist().name.ifBlank { "Untitled" } } }
                                    subtext {
                                        ::content {
                                            val p = playlist()
                                            val parts = mutableListOf<String>()
                                            parts.add("${p.photoFileNames.size} images")
                                            if (p.rules.isNotEmpty()) parts.add("${p.rules.size} rules")
                                            if (p.subPlaylists.isNotEmpty()) parts.add("${p.subPlaylists.size} sub-playlists")
                                            parts.joinToString(" | ")
                                        }
                                    }
                                }
                                shownWhen { activeId() == playlist()._id }.subtext {
                                    content = "Active"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
