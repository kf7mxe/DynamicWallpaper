package com.kf7mxe.autowall

import com.foodecision.sdk.hasSubscription
import com.foodecision.sdk.session
import com.foodecision.sdk.sessionToken
import com.lightningkite.kiteui.exceptions.installLsError
import com.lightningkite.kiteui.models.*
import com.lightningkite.kiteui.navigation.PageNavigator
import com.lightningkite.kiteui.reactive.*
import com.lightningkite.kiteui.views.ViewWriter
import com.lightningkite.kiteui.views.l2.appNav
import com.kf7mxe.autowall.pages.*
import com.kf7mxe.autowall.skills.ChatPage
import com.kf7mxe.autowall.skills.ChatPage.Welcome
import com.kf7mxe.autowall.skills.Interaction
import com.kf7mxe.autowall.skills.ToolResponse
import com.kf7mxe.autowall.theming.DynamicThemeManager
import com.kf7mxe.autowall.theming.PageContainerSemantic
import com.kf7mxe.autowall.theming.autoWallTheme
import com.lightningkite.kiteui.navigation.dialogPageNavigator
import com.lightningkite.kiteui.navigation.mainPageNavigator
import com.lightningkite.kiteui.navigation.pageNavigator
import com.lightningkite.kiteui.navigation.render
import com.lightningkite.kiteui.views.Element
import com.lightningkite.kiteui.views.atBottomCenter
import com.lightningkite.kiteui.views.atEnd
import com.lightningkite.kiteui.views.beforeSetup
import com.lightningkite.kiteui.views.card
import com.lightningkite.kiteui.views.centered
import com.lightningkite.kiteui.views.compact
import com.lightningkite.kiteui.views.direct.Frame
import com.lightningkite.kiteui.views.direct.button
import com.lightningkite.kiteui.views.direct.col
import com.lightningkite.kiteui.views.direct.frame
import com.lightningkite.kiteui.views.direct.h1
import com.lightningkite.kiteui.views.direct.icon
import com.lightningkite.kiteui.views.direct.link
import com.lightningkite.kiteui.views.direct.onClick
import com.lightningkite.kiteui.views.direct.padded
import com.lightningkite.kiteui.views.direct.row
import com.lightningkite.kiteui.views.direct.sizeConstraints
import com.lightningkite.kiteui.views.direct.space
import com.lightningkite.kiteui.views.direct.swapView
import com.lightningkite.kiteui.views.direct.swapping
import com.lightningkite.kiteui.views.direct.text
import com.lightningkite.kiteui.views.direct.unpadded
import com.lightningkite.kiteui.views.dynamicTheme
import com.lightningkite.kiteui.views.dynamicThemed
import com.lightningkite.kiteui.views.expanding
import com.lightningkite.kiteui.views.gravity
import com.lightningkite.kiteui.views.important
import com.lightningkite.kiteui.views.l2.appBase
import com.lightningkite.kiteui.views.l2.applySafeInsets
import com.lightningkite.kiteui.views.l2.icon
import com.lightningkite.kiteui.views.l2.navigatorViewDialog
import com.lightningkite.kiteui.views.l2.overlayFrame
import com.lightningkite.kiteui.views.minus
import com.lightningkite.kiteui.views.nav
import com.lightningkite.kiteui.views.padding
import com.lightningkite.kiteui.views.theme
import com.lightningkite.kiteui.views.themed
import com.lightningkite.reactive.context.invoke
import com.lightningkite.reactive.core.AppScope
import com.lightningkite.reactive.core.Signal
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

//val defaultTheme = Theme.flat2("default", Angle(0.55f))
val appTheme = Signal<Theme>(autoWallTheme(false))

val enableChatInterface = PersistentProperty("enableChatInterface",false)

val selectedPlaylist = PersistentProperty<Uuid?>("selectedPlaylist",null)


val chatHistory = Signal<List<Interaction>>(listOf(ToolResponse(Uuid.random(),"",Welcome())))



fun ViewWriter.app(navigator: PageNavigator, dialog: PageNavigator) {
    context.exceptionHandlers.installLsError()
    DynamicThemeManager.applyTheme()

    navigator.navigate(LibraryPage())

    return appBase(navigator,dialog) {
//        padding = 0.0
        themed(OuterSemantic).frame {
            padding = 0.rem
//            sessionToken.addListener {
//                if (sessionToken.value != null) AppScope.launch {
//                    session().me.invoke()?.muteBackupInfo?.let { muteBackupInfo.value = it }
//                    if (!hasSubscription()) mainPageNavigator.navigate(
//                        SubscriptionPage()
//                    )
//                    else mainPageNavigator.navigate(LandingPage())
//                }
//            }

            this.beforeSetup { applySafeInsets(bottom = true) }
            padding = 0.0.rem
//            navigator = mainPageNavigator
//            main.bindToPlatform(context)
//            mainPageNavigator = main
//            pageNavigator = main
//            dialogPageNavigator = dialog
            col {
                gap = 0.5.rem
                topBar(navigator)
//            paddingByEdge = Edges(0.5.rem,0.rem,0.5.rem,0.0.rem)
//            gap = 0.5.rem
//                 centered - sizeConstraints(maxWidth = 400.rem) -
//                screenViews(navigator)


                expanding.themed(PageContainerSemantic).swapView {
                    swapping(
                        transition = {
//                    ScreenTransition.Push
                            val newStack = navigator.stack.value
                            val transitionSet = theme.bodyTransitions
                            when {
                                newStack.size - navigator.stack.value.size > 0 -> transitionSet.forward
                                newStack.size - navigator.stack.value.size < 0 && newStack.firstOrNull() == navigator.stack.value.firstOrNull() -> transitionSet.reverse
                                else -> transitionSet.neutral
                            }
                        },
                        current = { navigator.currentPage() },
                        views = { screen ->
                            screen?.run {
                                render()
                            }
                        }
                    )
                }
                bottomBar(navigator)
            }
            navigatorViewDialog()
//            launch {
//                cleanUpOldTimers()
//                cleanUpOldCurrentRecipesGatheredIngredients()
//                cleanUpOldCurrentCompletedSteps()
//                autoSubtractPantryForPassedMeals()
//            }
//            connectivityDialog()
//            newFeaturesDialog()
            context.overlayFrame = this

        }
    }




//    return appNav(navigator, dialog) {
//        appName = "AutoWall"
//        ::navItems {
//            if(!enableChatInterface()) listOf(
//                NavLink(
//                    title = { "Home" },
//                    icon = { Icon.home }
//                ) { { HomePage() } },
//                NavLink(
//                    title = { "Library" },
//                    icon = { Icon.photos }
//                ) { { MyImagesPage() } },
//                NavLink(
//                    title = { "Explore" },
//                    icon = { Icon.search }
//                ) { { ExplorePage() } },
//                NavLink(
//                  title = {"Sync/Backup"},
//                    icon = { Icon.sync }
//                ){{ SyncBackupPage() }},
//                NavLink(
//                    title = { "Settings/Account" },
//                    icon = { Icon.settings }
//                ) { { ProfilePage() } },
//            ) else emptyList()
//        }
//        actions = listOf(
//            NavAction(title ={if(enableChatInterface()) "Normal Interface" else "Chat Interface"},icon = { if(enableChatInterface()) Icon.normalUi else Icon.experiment}) {
//                enableChatInterface.set(!enableChatInterface())
//                if(enableChatInterface()) mainPageNavigator.navigate(ChatPage())
//            }
//        )
//    }
}

fun ViewWriter.topBar(main: PageNavigator) {
// Nav 3 top and bottom (top)
    nav.unpadded.row {
        button {
            icon(Icon.arrowBack, "Go Back")
            ::visible { context.pageNavigator.canGoBack() }
            onClick { context.pageNavigator.goBack() }
        }
        centered.expanding.col {
            gap = 0.dp
            padding = 0.rem
            centered.h1 {
//                    gap = 0.dp
                padding = 0.rem
//                themeChoice += SatisfyTitleSemantic
                ::content {
                    context.mainPageNavigator.currentPage()?.title?.invoke()?:"AutoWall"
                }
            }
            centered.text {
//                    gap = 0.dp
                padding = 0.rem
//                themeChoice += SelectedPlanTitleSemantic
                ::content {
//                        val selectedPlanIdValue = session().me()?.selectedPlan ?: selectedPlanId()
//                        if (selectedPlanIdValue != null) {
//                            val planName = session().mealPlans.items().find { it._id == selectedPlanIdValue }?.name
//                            "Plan: ${planName ?: "Loading..."}"
//                        } else {
//                            "Plan: None"
//                        }
                    "Selected Playlist: "
                }
            }
        }
            centered.col {
                gap = 0.dp
                padding = 0.rem
                button {
                   centered.icon {
                       ::source {
                           if(enableChatInterface()) Icon.normalUi.copy(2.rem,2.rem) else Icon.experiment.copy(2.rem,2.rem)
                       }
                    }
                    onClick {
                        enableChatInterface.set(!enableChatInterface())
                        if(enableChatInterface()) context.mainPageNavigator.navigate(ChatPage())
                    }
                }
            }
    }
}

fun ViewWriter.screenViews(navigator: PageNavigator) {
//    centered.expanding.themed(PageContainerSemantic).col {
        val n = navigator
//        padding = 0.25.rem

//    }
}


fun ViewWriter.bottomBar(main: PageNavigator) {

    val navItems = listOf(
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
    )


       row {
           paddingByEdge = Edges(top = 0.5.rem, bottom = 0.5.rem, left = 0.rem, right = 0.rem)
           gap = 0.dp
           for (navLink in navItems) {
               expanding.link {
                   padding = 0.5.rem
                   dynamicThemed {
                       val test = context.mainPageNavigator.currentPage()
                           ?.let { context.mainPageNavigator.routes.render(it) }?.urlLikePath?.segments


                       val test2 = mainPageNavigator.routes.render(
                           navLink.destination.invoke(this)()
                       )?.urlLikePath?.segments

                       val matchingScreen = context.mainPageNavigator.currentPage()
                           ?.let { context.mainPageNavigator.routes.render(it) }?.urlLikePath?.segments == context.mainPageNavigator.routes.render(
                           navLink.destination.invoke(this)()
                       )?.urlLikePath?.segments
                       if (matchingScreen) SelectedSemantic else null
                   }
                   resetsStack = true
                   shown = false
                   ::shown { navLink.hidden?.invoke() != true }
                   col {
                       gap = 0.0.rem
                       centered.row {
                           centered.icon {
                               ::source { navLink.icon().copy(width = 1.5.rem, height = 1.5.rem) }
                               ::description { navLink.title() }
                           }
                           navLink.count?.let { count ->
                               centered.compact.frame {
                                   shown = false
                                   ::shown { count() != null }
                                   space(0.01)
                                   centered.text {
                                       ::content { count()?.takeIf { it > 0 }?.toString() ?: "" }
                                   }
                               }
                           }
                       }

                       val themeDerivation = ThemeDerivation {
                           it.copy(
                               "",
                               font = it.font.copy(size = 11.dp)
                           ).withoutBack
                       }
                       centered.themed(themeDerivation).text { ::content { navLink.title(this) } }
                   }
                   ::to { navLink.destination() }
               }
           }
   }
}