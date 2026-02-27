package com.dynamicwallpaper.pages

import com.dynamicwallpaper.Playlist
import com.dynamicwallpaper.storage.LocalPlaylistStore
import com.lightningkite.kiteui.Routable
import com.lightningkite.kiteui.models.*
import com.lightningkite.kiteui.navigation.Page
import com.lightningkite.kiteui.navigation.pageNavigator
import com.lightningkite.kiteui.reactive.*
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
