package com.kf7mxe.autowall.pages

import com.kf7mxe.autowall.Playlist
import com.kf7mxe.autowall.isPublic
import com.kf7mxe.autowall.name
import com.kf7mxe.autowall.sdk.createUnauthApi
import com.lightningkite.kiteui.Routable
import com.lightningkite.kiteui.models.*
import com.lightningkite.kiteui.navigation.Page
import com.lightningkite.kiteui.navigation.pageNavigator
import com.lightningkite.kiteui.reactive.*
import com.lightningkite.reactive.context.invoke
import com.lightningkite.kiteui.views.*
import com.lightningkite.kiteui.views.direct.*
import com.lightningkite.kiteui.views.direct.icon
import com.lightningkite.kiteui.views.l2.*
import com.lightningkite.reactive.core.Signal
import com.lightningkite.reactive.core.rememberSuspending
import com.lightningkite.services.database.Query
import com.lightningkite.services.database.and
import com.lightningkite.services.database.condition
import com.lightningkite.services.database.contains
import com.lightningkite.services.database.eq

@Routable("/browse-playlists")
class BrowsePlaylistsPage : Page {

    val searchQuery = Signal("")

    override fun ElementWriter.CanAddTheme.render() {
          val api = createUnauthApi()
        val isLoading = Signal(true)

        val playlists = rememberSuspending {
            try {
                val q = searchQuery()
                val result = if (q.isBlank()) {
                    api.playlist.query(Query<Playlist>(
                        condition = condition<Playlist> { it.isPublic eq true },
                        limit = 50
                    ))
                } else {
                    api.playlist.query(Query<Playlist>(
                        condition = condition<Playlist> { it.isPublic eq true } and condition<Playlist> { it.name.contains(q, ignoreCase = true) },
                        limit = 50
                    ))
                }
                isLoading.value = false
                result
            } catch (e: Exception) {
                isLoading.value = false
                toast("Failed to load playlists: ${e.message}")
                emptyList()
            }
        }

        scrolling.col {
            button {
                row {
                    icon(Icon.arrowBack, "Back")
                    text { content = "Back" }
                }
                onClick { pageNavigator.goBack() }
            }

            h2 { content = "Shared Playlists" }

            fieldTheme.textInput {
                hint = "Search playlists..."
                keyboardHints = KeyboardHints.title
                content bind searchQuery
            }

            space()

            // Loading indicator
            centered.shownWhen { isLoading() }.activityIndicator { }

            // Empty state
            centered.shownWhen { playlists().isEmpty() && !isLoading() }.col {
                icon(Icon.search.copy(width = 4.rem, height = 4.rem), "No results")
                space()
                h3 { content = "No Playlists Found" }
                subtext { ::content {
                    if (searchQuery().isNotBlank()) "No playlists match \"${searchQuery()}\""
                    else "No shared playlists available yet"
                } }
            }

            forEach(playlists) { playlist ->
                playlistCard(playlist)
            }
        }
    }
}
