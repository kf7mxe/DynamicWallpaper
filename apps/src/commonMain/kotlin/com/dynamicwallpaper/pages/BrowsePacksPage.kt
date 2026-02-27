package com.dynamicwallpaper.pages

import com.dynamicwallpaper.Pack
import com.dynamicwallpaper.name
import com.dynamicwallpaper.sdk.createUnauthApi
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
import com.lightningkite.services.database.Condition
import com.lightningkite.services.database.Query
import com.lightningkite.services.database.condition
import com.lightningkite.services.database.contains

@Routable("/browse-packs")
class BrowsePacksPage : Page {

    val searchQuery = Signal("")

    override fun ViewWriter.render() {
        val api = createUnauthApi()

        val packs = rememberSuspending {
            try {
                val q = searchQuery()
                if (q.isBlank()) {
                    api.pack.query(Query<Pack>(limit = 50))
                } else {
                    api.pack.query(Query<Pack>(
                        condition = condition<Pack> { it.name.contains(q, ignoreCase = true) },
                        limit = 50
                    ))
                }
            } catch (e: Exception) {
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

            forEach(packs) { pack ->
                packCard(pack)
            }
        }
    }
}
