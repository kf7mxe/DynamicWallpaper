package com.dynamicwallpaper.pages

import com.dynamicwallpaper.Pack
import com.dynamicwallpaper.Playlist
import com.dynamicwallpaper.isFeatured
import com.dynamicwallpaper.isPublic
import com.dynamicwallpaper.sdk.createUnauthApi
import com.dynamicwallpaper.storage.packPreviewUrl
import com.dynamicwallpaper.storage.playlistPreviewUrl
import com.lightningkite.kiteui.Routable
import com.lightningkite.kiteui.models.*
import com.lightningkite.kiteui.navigation.Page
import com.lightningkite.kiteui.navigation.pageNavigator
import com.lightningkite.kiteui.reactive.*
import com.lightningkite.kiteui.views.*
import com.lightningkite.kiteui.views.direct.*
import com.lightningkite.kiteui.views.l2.*
import com.lightningkite.reactive.core.Signal
import com.lightningkite.reactive.core.rememberSuspending
import com.lightningkite.reactive.context.invoke
import com.lightningkite.services.database.Query
import com.lightningkite.services.database.condition
import com.lightningkite.services.database.eq

@Routable("/explore")
class ExplorePage : Page {

    val searchQuery = Signal("")
    val selectedTag = Signal<String?>(null)

    override fun ViewWriter.render() {
        val api = createUnauthApi()

        val featuredPacks = rememberSuspending {
            try {
                api.pack.query(Query<Pack>(condition = condition<Pack> { it.isFeatured eq true }, limit = 5))
            } catch (e: Exception) {
                emptyList()
            }
        }

        val publicPlaylists = rememberSuspending {
            try {
                api.playlist.query(Query<Playlist>(condition = condition<Playlist> { it.isPublic eq true }, limit = 10))
            } catch (e: Exception) {
                emptyList()
            }
        }

        val allPacks = rememberSuspending {
            try {
                api.pack.query(Query<Pack>(limit = 20))
            } catch (e: Exception) {
                emptyList()
            }
        }

        // Collect unique tags from all fetched data
        val allTags = rememberSuspending {
            val packTags = (featuredPacks() + allPacks()).flatMap { it.tags }
            val playlistTags = publicPlaylists().flatMap { it.tags }
            (packTags + playlistTags).distinct().sorted()
        }

        scrolling.col {
            h2 { content = "Explore" }

            // Search bar
            fieldTheme.textInput {
                hint = "Search packs and playlists..."
                keyboardHints = KeyboardHints.title
                content bind searchQuery
            }

            space()

            // Tag filter chips
            row {
                button {
                    text { ::content { if (selectedTag() == null) "All" else "All" } }
                    onClick { selectedTag.value = null }
                }
                forEach(allTags) { tag ->
                    button {
                        text { content = tag }
                        onClick { selectedTag.value = tag }
                    }
                }
            }

            space()

            // Featured section
            h3 { content = "Featured" }
            subtext { content = "Hand-picked wallpaper packs" }
            col {
                forEach(featuredPacks) { pack ->
                    featuredPackCard(pack)
                }
            }

            space()

            // Packs section
            row {
                expanding.h3 { content = "Wallpaper Packs" }
                button {
                    text { content = "See All" }
                    onClick { pageNavigator.navigate(BrowsePacksPage()) }
                }
            }
            col {
                forEach(allPacks) { pack ->
                    packCard(pack)
                }
            }

            space()

            // Playlists section
            row {
                expanding.h3 { content = "Shared Playlists" }
                button {
                    text { content = "See All" }
                    onClick { pageNavigator.navigate(BrowsePlaylistsPage()) }
                }
            }
            col {
                forEach(publicPlaylists) { playlist ->
                    playlistCard(playlist)
                }
            }
        }
    }
}

fun ViewWriter.featuredPackCard(pack: Pack) {
    card.col {
        button {
            col {
                val previewUrl = packPreviewUrl(pack)
                if (previewUrl != null) {
                    sizeConstraints(height = 12.rem).image {
                        source = ImageRemote(previewUrl)
                        scaleType = ImageScaleType.Crop
                    }
                }
                h3 { content = pack.name.ifBlank { "Untitled Pack" } }
                if (pack.description.isNotBlank()) {
                    subtext { content = pack.description.take(150) }
                }
                row {
                    subtext { content = "${pack.imageFileNames.size} images" }
                    subtext { content = "${pack.downloadCount} downloads" }
                    if (pack.isFree) {
                        subtext { content = "Free" }
                    }
                }
            }
            onClick { pageNavigator.navigate(PackDetailPage(pack._id)) }
        }
    }
}

fun ViewWriter.packCard(pack: Pack) {
    card.col {
        button {
            row {
                val previewUrl = packPreviewUrl(pack)
                if (previewUrl != null) {
                    sizeConstraints(width = 5.rem, height = 5.rem).image {
                        source = ImageRemote(previewUrl)
                        scaleType = ImageScaleType.Crop
                    }
                }
                expanding.col {
                    text { content = pack.name.ifBlank { "Untitled Pack" } }
                    if (pack.description.isNotBlank()) {
                        subtext { content = pack.description.take(80) }
                    }
                    row {
                        subtext { content = "${pack.imageFileNames.size} images" }
                        if (pack.isFree) {
                            subtext { content = "Free" }
                        }
                    }
                }
            }
            onClick { pageNavigator.navigate(PackDetailPage(pack._id)) }
        }
    }
}

fun ViewWriter.playlistCard(playlist: Playlist) {
    card.col {
        button {
            row {
                val previewUrl = playlistPreviewUrl(playlist)
                if (previewUrl != null) {
                    sizeConstraints(width = 5.rem, height = 5.rem).image {
                        source = ImageRemote(previewUrl)
                        scaleType = ImageScaleType.Crop
                    }
                }
                expanding.col {
                    text { content = playlist.name.ifBlank { "Untitled Playlist" } }
                    if (playlist.description.isNotBlank()) {
                        subtext { content = playlist.description.take(80) }
                    }
                    row {
                        subtext { content = "${playlist.photoFileNames.size} images" }
                        if (playlist.rules.isNotEmpty()) {
                            subtext { content = "${playlist.rules.size} rules" }
                        }
                    }
                }
            }
            onClick { pageNavigator.navigate(SharedPlaylistDetailPage(playlist._id)) }
        }
    }
}
