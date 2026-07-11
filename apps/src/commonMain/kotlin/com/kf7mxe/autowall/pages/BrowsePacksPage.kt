package com.kf7mxe.autowall.pages

import com.foodecision.sdk.session
import com.kf7mxe.autowall.WallpaperPack
import com.kf7mxe.autowall.name
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
import com.lightningkite.reactive.core.remember
import com.lightningkite.reactive.core.rememberSuspending
import com.lightningkite.services.database.Condition
import com.lightningkite.services.database.Query
import com.lightningkite.services.database.condition
import com.lightningkite.services.database.contains
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Routable("/browse-packs")
class BrowsePacksPage : Page {

    val searchQuery = Signal("")

    override fun ElementWriter.CanAddTheme.render() {

        val packs = rememberSuspending {
                session().serverCached?.storeWallpaperPacks?.query(Query(condition {
                    Condition.And(
                        listOfNotNull(
                            searchQuery().takeUnless { it.isBlank() }?.let { Condition.FullTextSearch(it)}
                        )
                    )
                }))

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
            centered.shownWhen { !packs.state.ready }.activityIndicator { }

            // Empty state
            centered.shownWhen { packs()?.invoke()?.isEmpty() == true }.col {
                icon(Icon.search.copy(width = 4.rem, height = 4.rem), "No results")
                space()
                h3 { content = "No Packs Found" }
                subtext { ::content {
                    if (searchQuery().isNotBlank()) "No packs match \"${searchQuery()}\""
                    else "No wallpaper packs available yet"
                } }
            }

            lazyColumn(
                items = remember { packs()?.invoke() ?: emptyList() },
                id = {it._id},
                loadMore = { packs()?.invoke()?.let { packs()?.limit = packs()!!.invoke().size + 20; delay(3.seconds)}},
                render = {pack ->
                    card.col {
                        button {
                            row {
//                                val previewUrl = packPreviewUrl(wallpaperPack)
//                                if (previewUrl != null) {
//                                    sizeConstraints(width = 5.rem, height = 5.rem).image {
//                                        source = ImageRemote(previewUrl)
//                                        scaleType = ImageScaleType.Crop
//                                    }
//                                }
//                                expanding.col {
//                                    text { content = wallpaperPack.name.ifBlank { "Untitled Pack" } }
//                                    if (wallpaperPack.description.isNotBlank()) {
//                                        subtext { content = wallpaperPack.description.take(80) }
//                                    }
//                                    row {
//                                        subtext { content = "${wallpaperPack.wallpapers.size} images" }
////                        if (wallpaperPack.isFree) {
////                            subtext { content = "Free" }
////                        }
//                                    }
//                                }
                            }
//                            onClick { pageNavigator.navigate(PackDetailPage(wallpaperPack._id)) }
                        }
                    }

                }
            )
        }
    }
}
