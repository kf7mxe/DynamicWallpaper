import com.kf7mxe.autowall.AutoRoutes
import com.lightningkite.kiteui.navigation.PageNavigator
import com.kf7mxe.autowall.app
import com.kf7mxe.autowall.appTheme
import com.lightningkite.kiteui.root

fun main() {
    root(appTheme.value) {
        app(PageNavigator { AutoRoutes }, PageNavigator { AutoRoutes })
    }
}
