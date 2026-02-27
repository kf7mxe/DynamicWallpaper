package com.dynamicwallpaper.pages

import com.dynamicwallpaper.photo
import com.lightningkite.kiteui.Routable
import com.lightningkite.kiteui.models.*
import com.lightningkite.kiteui.navigation.Page
import com.lightningkite.kiteui.views.*
import com.lightningkite.kiteui.views.direct.*
import com.lightningkite.kiteui.views.l2.*

@Routable("/my-images")
class MyImagesPage : Page {
    override fun ViewWriter.render() {
        col {
            h2 { content = "My Images" }
            subtext { content = "Manage your wallpaper image library." }

            space()

            centered.col {
                icon(Icon.photo.copy(width = 4.rem, height = 4.rem), "No images")
                space()
                h3 { content = "No Images Yet" }
                subtext { content = "Import images from the gallery or download packs from the Explore tab." }
                space()
                important.button {
                    row {
                        icon(Icon.add, "Import")
                        text { content = "Import from Gallery" }
                    }
                    onClick {
                        // TODO: Open image picker
                    }
                }
            }
        }
    }
}
