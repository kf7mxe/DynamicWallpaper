package com.dynamicwallpaper.pages

import com.dynamicwallpaper.Playlist
import com.dynamicwallpaper.sdk.createUnauthApi
import com.dynamicwallpaper.storage.ImageDownloader
import com.dynamicwallpaper.storage.LocalPlaylistStore
import com.dynamicwallpaper.storage.playlistPreviewUrl
import com.dynamicwallpaper.storage.serverFileUrl
import com.lightningkite.kiteui.Routable
import com.lightningkite.kiteui.models.*
import com.lightningkite.kiteui.navigation.Page
import com.lightningkite.kiteui.navigation.pageNavigator
import com.lightningkite.kiteui.reactive.*
import com.lightningkite.reactive.context.invoke
import com.lightningkite.reactive.core.Signal
import com.lightningkite.kiteui.views.*
import com.lightningkite.kiteui.views.direct.*
import com.lightningkite.kiteui.views.l2.*
import com.lightningkite.reactive.core.rememberSuspending
import kotlin.uuid.Uuid

@Routable("/shared-playlist/{playlistId}")
class SharedPlaylistDetailPage(val playlistId: Uuid) : Page {

    override fun ViewWriter.render() {
        val api = createUnauthApi()

        val playlistData = rememberSuspending {
            try {
                api.playlist.detail(playlistId)
            } catch (e: Exception) {
                null
            }
        }

        val imageUrls = rememberSuspending {
            playlistData()?.photoFileNames?.map { serverFileUrl(it) } ?: emptyList()
        }

        val downloadProgress = Signal<ImageDownloader.DownloadProgress?>(null)
        val isDownloading = Signal(false)

        scrolling.col {
            button {
                row {
                    icon(Icon.arrowBack, "Back")
                    text { content = "Back" }
                }
                onClick { pageNavigator.goBack() }
            }

            space()

            // Hero image
            col {
                val previewUrl = rememberSuspending { playlistData()?.let { playlistPreviewUrl(it) } }
                shownWhen { previewUrl() != null }.sizeConstraints(height = 14.rem).image {
                    ::source { previewUrl()?.let { ImageRemote(it) } ?: ImageRemote("") }
                    scaleType = ImageScaleType.Crop
                }
            }

            space()

            h2 { ::content { playlistData()?.name ?: "Loading..." } }

            subtext { ::content { playlistData()?.description ?: "" } }

            space()

            // Stats row
            row {
                subtext { ::content { "${playlistData()?.photoFileNames?.size ?: 0} images" } }
                subtext { ::content { "${playlistData()?.downloadCount ?: 0} downloads" } }
            }

            // Tags
            row {
                forEach(rememberSuspending { playlistData()?.tags ?: emptyList() }) { tag ->
                    subtext { content = tag }
                }
            }

            space()

            // Download button with progress
            important.button {
                text {
                    ::content {
                        val prog = downloadProgress()
                        when {
                            prog != null -> "Downloading ${prog.currentIndex + 1}/${prog.totalCount}..."
                            isDownloading() -> "Preparing..."
                            else -> "Download Playlist"
                        }
                    }
                }
                action = Action("Download") {
                    val p = playlistData() ?: return@Action
                    isDownloading.value = true
                    try {
                        val newPlaylist = Playlist(
                            name = p.name,
                            description = p.description,
                            tags = p.tags,
                            rules = p.rules,
                            subPlaylists = p.subPlaylists,
                        )
                        LocalPlaylistStore.save(newPlaylist)
                        val imageIds = ImageDownloader.downloadPlaylistImages(
                            playlist = p,
                            targetPlaylistId = newPlaylist._id.toString(),
                            onProgress = { downloadProgress.value = it },
                        )
                        LocalPlaylistStore.save(newPlaylist.copy(photoFileNames = imageIds))
                        toast("Downloaded '${p.name}' with ${imageIds.size} images")
                        pageNavigator.goBack()
                    } catch (e: Exception) {
                        toast("Download failed: ${e.message}")
                    } finally {
                        isDownloading.value = false
                        downloadProgress.value = null
                    }
                }
            }

            space()

            // Rules summary
            col {
                forEach(rememberSuspending { playlistData()?.rules ?: emptyList() }) { rule ->
                    card.row {
                        expanding.col {
                            text { content = "${rule.trigger.displayName} → ${rule.action.displayName}" }
                        }
                    }
                }
            }

            // Sub-playlists
            col {
                forEach(rememberSuspending { playlistData()?.subPlaylists ?: emptyList() }) { sub ->
                    card.row {
                        expanding.col {
                            text { content = sub.name.ifBlank { "Untitled" } }
                            subtext { content = "${sub.fileNames.size} images" }
                        }
                    }
                }
            }

            space()

            // Image previews
            h3 { content = "Images" }
            col {
                forEach(imageUrls) { url ->
                    sizeConstraints(height = 10.rem).button {
                        image {
                            source = ImageRemote(url)
                            scaleType = ImageScaleType.Crop
                        }
                        onClick {
                            pageNavigator.navigate(ImagePreviewPage(url))
                        }
                    }
                }
            }
        }
    }
}
