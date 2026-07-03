package com.kf7mxe.autowall

import com.lightningkite.kiteui.exceptions.installLsError
import com.lightningkite.kiteui.models.*
import com.lightningkite.kiteui.navigation.PageNavigator
import com.lightningkite.kiteui.reactive.*
import com.lightningkite.kiteui.views.ViewWriter
import com.lightningkite.kiteui.views.l2.appNav
import com.kf7mxe.autowall.pages.*
import com.kf7mxe.autowall.theming.DynamicThemeManager
import com.lightningkite.kiteui.navigation.mainPageNavigator
import com.lightningkite.reactive.context.invoke
import com.lightningkite.reactive.core.Signal

val defaultTheme = Theme.flat2("default", Angle(0.55f))
val appTheme = Signal<Theme>(defaultTheme)

val enableChatInterface = PersistentProperty("enableChatInterface",false)

fun ViewWriter.app(navigator: PageNavigator, dialog: PageNavigator) {
    context.exceptionHandlers.installLsError()
    DynamicThemeManager.applyTheme()

    navigator.navigate(LibraryPage())

    return appNav(navigator, dialog) {
        appName = "AutoWall"
        ::navItems {
            if(!enableChatInterface()) listOf(
                NavLink(
                    title = { "Home" },
                    icon = { Icon.home }
                ) { { HomePage() } },
                NavLink(
                    title = { "Library" },
                    icon = { Icon.photos }
                ) { { MyImagesPage() } },
                NavLink(
                    title = { "Explore" },
                    icon = { Icon.search }
                ) { { ExplorePage() } },
                NavLink(
                  title = {"Sync/Backup"},
                    icon = { Icon.sync }
                ){{ SyncBackupPage() }},
                NavLink(
                    title = { "Settings/Account" },
                    icon = { Icon.settings }
                ) { { ProfilePage() } },
            ) else emptyList()
        }
        actions = listOf(
            NavAction(title ={if(enableChatInterface()) "Normal Interface" else "Chat Interface"},icon = { if(enableChatInterface()) Icon.normalUi else Icon.experiment}) {
                enableChatInterface.set(!enableChatInterface())
                if(enableChatInterface()) mainPageNavigator.navigate(ChatPage())
            }
        )
    }
}
