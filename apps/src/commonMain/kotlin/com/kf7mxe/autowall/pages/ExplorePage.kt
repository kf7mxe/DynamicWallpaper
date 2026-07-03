package com.kf7mxe.autowall.pages

import com.kf7mxe.autowall.WallpaperPack
import com.kf7mxe.autowall.Playlist
import com.kf7mxe.autowall.isFeatured
import com.kf7mxe.autowall.isPublic
import com.kf7mxe.autowall.sdk.createUnauthApi
import com.kf7mxe.autowall.storage.packPreviewUrl
import com.kf7mxe.autowall.storage.playlistPreviewUrl
import com.lightningkite.kiteui.Routable
import com.lightningkite.kiteui.models.*
import com.lightningkite.kiteui.navigation.Page
import com.lightningkite.kiteui.navigation.pageNavigator
import com.lightningkite.kiteui.reactive.*
import com.lightningkite.kiteui.views.*
import com.lightningkite.kiteui.views.direct.*
import com.lightningkite.kiteui.views.l2.*
import com.lightningkite.kiteui.views.l2.toast
import com.lightningkite.reactive.core.Signal
import com.lightningkite.reactive.core.rememberSuspending
import com.lightningkite.reactive.context.invoke
import com.lightningkite.services.database.Query
import com.lightningkite.services.database.condition
import com.lightningkite.services.database.eq
import kotlin.time.Duration.Companion.seconds

@Routable("/explore")
class ExplorePage : Page {

    val searchQuery = Signal("")
    val selectedTag = Signal<String?>(null)

    override fun ElementWriter.CanAddTheme.render() {
        val api = createUnauthApi()
        val isLoading = Signal(true)
        val errorMessage = Signal<String?>(null)

        val featuredPacks = rememberSuspending {
            try {
                api.pack.query(Query<WallpaperPack>(condition = condition<WallpaperPack> { it.isFeatured eq true }, limit = 5))
            } catch (e: Exception) {
                errorMessage.value = "Failed to load featured packs: ${e.message}"
                this@render.context.toast("Failed to load explore data", 3.seconds)
                emptyList()
            }
        }

        val publicPlaylists = rememberSuspending {
            try {
                api.playlist.query(Query<Playlist>(condition = condition<Playlist> { it.isPublic eq true }, limit = 10))
            } catch (e: Exception) {
                errorMessage.value = "Failed to load playlists: ${e.message}"
                emptyList()
            }
        }

        val allPacks = rememberSuspending {
            try {
                val result = api.pack.query(Query<WallpaperPack>(limit = 20))
                isLoading.value = false
                result
            } catch (e: Exception) {
                isLoading.value = false
                errorMessage.value = "Failed to load packs: ${e.message}"
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

            // Loading indicator
            centered.shownWhen { isLoading() }.activityIndicator { }

            // Tag filter chips
            shownWhen { !isLoading() }.row {
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
            shownWhen { !isLoading() }.col {
                h3 { content = "Featured" }
                subtext { content = "Hand-picked wallpaper packs" }

                centered.shownWhen { featuredPacks().isEmpty() && !isLoading() }.col {
                    subtext { content = "No featured packs available" }
                }

                col {
                    forEach(featuredPacks) { pack ->
                        featuredPackCard(pack)
                    }
                }
            }

            space()

            // Packs section
            shownWhen { !isLoading() }.col {
                row {
                    expanding.h3 { content = "Wallpaper Packs" }
                    button {
                        text { content = "See All" }
                        onClick { context.pageNavigator.navigate(BrowsePacksPage()) }
                    }
                }

                centered.shownWhen { allPacks().isEmpty() && !isLoading() }.col {
                    subtext { content = "No packs found" }
                }

                col {
                    forEach(allPacks) { pack ->
                        packCard(pack)
                    }
                }
            }

            space()

            // Playlists section
            shownWhen { !isLoading() }.col {
                row {
                    expanding.h3 { content = "Shared Playlists" }
                    button {
                        text { content = "See All" }
                        onClick { context.pageNavigator.navigate(BrowsePlaylistsPage()) }
                    }
                }

                centered.shownWhen { publicPlaylists().isEmpty() && !isLoading() }.col {
                    subtext { content = "No shared playlists found" }
                }

                col {
                    forEach(publicPlaylists) { playlist ->
                        playlistCard(playlist)
                    }
                }
            }
        }
    }
}

fun ViewWriter.featuredPackCard(wallpaperPack: WallpaperPack) {
    card.col {
        button {
            col {
                val previewUrl = packPreviewUrl(wallpaperPack)
                if (previewUrl != null) {
                    sizeConstraints(height = 12.rem).image {
                        source = ImageRemote(previewUrl)
                        scaleType = ImageScaleType.Crop
                    }
                }
                h3 { content = wallpaperPack.name.ifBlank { "Untitled Pack" } }
                if (wallpaperPack.description.isNotBlank()) {
                    subtext { content = wallpaperPack.description.take(150) }
                }
                row {
                    subtext { content = "${wallpaperPack.imageFileNames.size} images" }
                    subtext { content = "${wallpaperPack.downloadCount} downloads" }
                    if (wallpaperPack.isFree) {
                        subtext { content = "Free" }
                    }
                }
            }
            onClick { pageNavigator.navigate(PackDetailPage(wallpaperPack._id)) }
        }
    }
}

fun ViewWriter.packCard(wallpaperPack: WallpaperPack) {
    card.col {
        button {
            row {
                val previewUrl = packPreviewUrl(wallpaperPack)
                if (previewUrl != null) {
                    sizeConstraints(width = 5.rem, height = 5.rem).image {
                        source = ImageRemote(previewUrl)
                        scaleType = ImageScaleType.Crop
                    }
                }
                expanding.col {
                    text { content = wallpaperPack.name.ifBlank { "Untitled Pack" } }
                    if (wallpaperPack.description.isNotBlank()) {
                        subtext { content = wallpaperPack.description.take(80) }
                    }
                    row {
                        subtext { content = "${wallpaperPack.imageFileNames.size} images" }
                        if (wallpaperPack.isFree) {
                            subtext { content = "Free" }
                        }
                    }
                }
            }
            onClick { pageNavigator.navigate(PackDetailPage(wallpaperPack._id)) }
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
