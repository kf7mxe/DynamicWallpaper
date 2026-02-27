package com.dynamicwallpaper

import com.lightningkite.kiteui.*
import com.lightningkite.kiteui.exceptions.ExceptionToMessages
import com.lightningkite.kiteui.exceptions.installLsError
import com.lightningkite.kiteui.models.*
import com.lightningkite.kiteui.navigation.PageNavigator
import com.lightningkite.kiteui.reactive.*
import com.lightningkite.kiteui.views.ViewWriter
import com.lightningkite.kiteui.views.l2.appNav
import com.dynamicwallpaper.pages.*
import com.dynamicwallpaper.theming.DynamicThemeManager
import com.lightningkite.reactive.core.Signal

val defaultTheme = Theme.flat2("default", Angle(0.55f))
val appTheme = Signal<Theme>(defaultTheme)

fun ViewWriter.app(navigator: PageNavigator, dialog: PageNavigator) {
    ExceptionToMessages.root.installLsError()

    DynamicThemeManager.applyTheme()

    navigator.navigate(PlaylistsPage())

    return appNav(navigator, dialog) {
        appName = "Dynamic Wallpaper"
        ::navItems {
            listOf(
                NavLink(
                    title = { "Playlists" },
                    icon = { Icon.list }
                ) { { PlaylistsPage() } },
                NavLink(
                    title = { "My Images" },
                    icon = { Icon.photos }
                ) { { MyImagesPage() } },
                NavLink(
                    title = { "Explore" },
                    icon = { Icon.search }
                ) { { ExplorePage() } },
                NavLink(
                    title = { "Profile" },
                    icon = { Icon.settings }
                ) { { ProfilePage() } },
            )
        }
    }
}
