package com.dynamicwallpaper

import com.lightningkite.kiteui.navigation.PageNavigator
import platform.UIKit.UIViewController

fun root(viewController: UIViewController) {
    viewController.setup(appTheme) {
        app(
            PageNavigator { AutoRoutes },
            PageNavigator { AutoRoutes }
        )
    }
}
