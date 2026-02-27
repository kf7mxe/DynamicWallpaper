import com.lightningkite.kiteui.navigation.PageNavigator
import com.dynamicwallpaper.app
import com.dynamicwallpaper.appTheme
import com.dynamicwallpaper.pages.AutoRoutes
import com.lightningkite.kiteui.root

fun main() {
    root(appTheme.value) {
        app(PageNavigator { AutoRoutes }, PageNavigator { AutoRoutes })
    }
}
