package com.dynamicwallpaper.pages

import com.lightningkite.kiteui.Routable
import com.lightningkite.kiteui.models.*
import com.lightningkite.kiteui.navigation.Page
import com.lightningkite.kiteui.navigation.pageNavigator
import com.lightningkite.kiteui.reactive.*
import com.lightningkite.kiteui.views.*
import com.lightningkite.kiteui.views.direct.*
import com.lightningkite.kiteui.views.l2.*

data class TriggerOption(
    val name: String,
    val description: String,
    val navigate: () -> Page,
)

@Routable("/playlists/{playlistId}/rules/trigger")
class SelectTriggerPage(val playlistId: String) : Page {

    override fun ViewWriter.render() {
        val options = listOf(
            TriggerOption(
                "Time Interval",
                "Change wallpaper at regular intervals",
            ) { ConfigureTimeIntervalPage(playlistId) },
            TriggerOption(
                "Specific Date",
                "Change wallpaper on a specific date each year",
            ) { ConfigureDateTriggerPage(playlistId) },
            TriggerOption(
                "By Season",
                "Change wallpaper based on the current season",
            ) { ConfigureSeasonTriggerPage(playlistId) },
            TriggerOption(
                "By Location",
                "Change wallpaper when entering or leaving a location",
            ) { ConfigureLocationTriggerPage(playlistId) },
            TriggerOption(
                "By Weather",
                "Change wallpaper based on weather conditions",
            ) { ConfigureWeatherTriggerPage(playlistId) },
        )

        scrolling.col {
            h2 { content = "Select Trigger" }
            subtext { content = "Choose what should trigger a wallpaper change." }

            space()

            for (option in options) {
                card.button {
                    row {
                        expanding.col {
                            text { content = option.name }
                            subtext { content = option.description }
                        }
                        icon(Icon.chevronRight, "Go")
                    }
                    onClick {
                        pageNavigator.navigate(option.navigate())
                    }
                }
                space()
            }

            button {
                text { content = "Cancel" }
                onClick { pageNavigator.goBack() }
            }
        }
    }
}
