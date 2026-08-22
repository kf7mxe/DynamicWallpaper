package com.kf7mxe.autowall.skills

import com.lightningkite.kiteui.views.ElementWriter
import com.lightningkite.kiteui.views.direct.col
import com.lightningkite.kiteui.views.direct.row
import com.lightningkite.kiteui.views.direct.text

class ViewPlaylistsTool : Tool {
    override val name = "View Playlists"
    // collapsesInHistory defaults to false (Lightweight)

    override fun matches(query: String) = query == "view playlists"

    override fun ElementWriter.CanAddTheme.render(query: String?,params: Map<String, Any>?) {
        col {
            text("🎵 Your Playlists (Lightweight UI - Stays open forever)")
            text("- Rock Anthems")
            text("- Jazz Focus")
        }
    }
}

class ViewPhotoPacksTool : Tool {
    override val name = "View Photo Packs"
//    override val collapsesInHistory = true // Heavy UI! Will auto-collapse.

    override fun matches(query: String) = query == "view photos"

    override fun ElementWriter.CanAddTheme.render(query: String?,params: Map<String, Any>?) {
        col {
            text("🖼️ Loading High-Res Photo Grid... (Heavy UI)")
            // Imagine a large grid of images or a memory-heavy map here
            row { text("[Photo 1] [Photo 2] [Photo 3]") }
        }
    }
}