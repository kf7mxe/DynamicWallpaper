package com.dynamicwallpaper.pages

import com.lightningkite.kiteui.Routable
import com.lightningkite.kiteui.models.*
import com.lightningkite.kiteui.navigation.Page
import com.lightningkite.kiteui.navigation.pageNavigator
import com.lightningkite.kiteui.views.*
import com.lightningkite.kiteui.views.direct.*
import com.lightningkite.kiteui.views.l2.*

@Routable("/image-preview/{encodedUrl}")
class ImagePreviewPage(val encodedUrl: String) : Page {

    override fun ViewWriter.render() {
        frame {
            image {
                source = ImageRemote(encodedUrl)
                scaleType = ImageScaleType.Fit
            }
            atTopStart.button {
                icon(Icon.close, "Close")
                onClick { pageNavigator.goBack() }
            }
        }
    }
}
