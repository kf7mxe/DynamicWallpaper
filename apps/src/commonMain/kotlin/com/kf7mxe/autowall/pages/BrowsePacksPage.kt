package com.kf7mxe.autowall.pages

import com.kf7mxe.autowall.WallpaperPack
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
import com.lightningkite.services.database.condition
import com.lightningkite.services.database.contains

@Routable("/browse-packs")
class BrowsePacksPage : Page {

    val searchQuery = Signal("")

    override fun ElementWriter.CanAddTheme.render() {
        val api = createUnauthApi()
        val isLoading = Signal(true)

        val packs = rememberSuspending {
            try {
                val q = searchQuery()
                val result = if (q.isBlank()) {
                    api.pack.query(Query<WallpaperPack>(limit = 50))
                } else {
                    api.pack.query(Query<WallpaperPack>(
                        condition = condition<WallpaperPack> { it.name.contains(q, ignoreCase = true) },
                        limit = 50
                    ))
                }
                isLoading.value = false
                result
            } catch (e: Exception) {
                isLoading.value = false
                toast("Failed to load packs: ${e.message}")
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

            h2 { content = "Wallpaper Packs" }

            fieldTheme.textInput {
                hint = "Search packs..."
                keyboardHints = KeyboardHints.title
                content bind searchQuery
            }

            space()

            // Loading indicator
            centered.shownWhen { isLoading() }.activityIndicator { }

            // Empty state
            centered.shownWhen { packs().isEmpty() && !isLoading() }.col {
                icon(Icon.search.copy(width = 4.rem, height = 4.rem), "No results")
                space()
                h3 { content = "No Packs Found" }
                subtext { ::content {
                    if (searchQuery().isNotBlank()) "No packs match \"${searchQuery()}\""
                    else "No wallpaper packs available yet"
                } }
            }

            forEach(packs) { pack ->
                packCard(pack)
            }
        }
    }
}
