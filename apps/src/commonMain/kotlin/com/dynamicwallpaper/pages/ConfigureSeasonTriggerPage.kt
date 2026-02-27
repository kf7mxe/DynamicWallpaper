package com.dynamicwallpaper.pages

import com.dynamicwallpaper.Season
import com.dynamicwallpaper.TriggerBySeason
import com.lightningkite.kiteui.Routable
import com.lightningkite.kiteui.exceptions.PlainTextException
import com.lightningkite.kiteui.models.*
import com.lightningkite.kiteui.navigation.Page
import com.lightningkite.kiteui.navigation.pageNavigator
import com.lightningkite.kiteui.reactive.*
import com.lightningkite.kiteui.views.*
import com.lightningkite.kiteui.views.direct.*
import com.lightningkite.kiteui.views.l2.*
import com.lightningkite.reactive.core.Signal
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

private data class SeasonConfig(
    val name: String,
    val defaultStartMonth: Int, val defaultStartDay: Int,
    val defaultEndMonth: Int, val defaultEndDay: Int,
)

private val defaultSeasons = listOf(
    SeasonConfig("Winter", 11, 21, 2, 19),
    SeasonConfig("Spring", 2, 20, 5, 20),
    SeasonConfig("Summer", 5, 21, 8, 21),
    SeasonConfig("Fall", 8, 22, 11, 20),
)

@Routable("/playlists/{playlistId}/rules/trigger/season")
class ConfigureSeasonTriggerPage(val playlistId: String) : Page {

    // Each season: enabled, startMonth, startDay, endMonth, endDay
    val winterEnabled = Signal(false)
    val winterStartMonth = Signal(11)
    val winterStartDay = Signal("21")
    val winterEndMonth = Signal(2)
    val winterEndDay = Signal("19")

    val springEnabled = Signal(false)
    val springStartMonth = Signal(2)
    val springStartDay = Signal("20")
    val springEndMonth = Signal(5)
    val springEndDay = Signal("20")

    val summerEnabled = Signal(false)
    val summerStartMonth = Signal(5)
    val summerStartDay = Signal("21")
    val summerEndMonth = Signal(8)
    val summerEndDay = Signal("21")

    val fallEnabled = Signal(false)
    val fallStartMonth = Signal(8)
    val fallStartDay = Signal("22")
    val fallEndMonth = Signal(11)
    val fallEndDay = Signal("20")

    override fun ViewWriter.render() {
        scrolling.col {
            h2 { content = "Season Trigger" }
            subtext { content = "Change wallpaper based on the current season." }

            space()

            seasonCard("Winter", winterEnabled, winterStartMonth, winterStartDay, winterEndMonth, winterEndDay)
            space()
            seasonCard("Spring", springEnabled, springStartMonth, springStartDay, springEndMonth, springEndDay)
            space()
            seasonCard("Summer", summerEnabled, summerStartMonth, summerStartDay, summerEndMonth, summerEndDay)
            space()
            seasonCard("Fall", fallEnabled, fallStartMonth, fallStartDay, fallEndMonth, fallEndDay)

            space()

            // Action buttons
            row {
                expanding.button {
                    text { content = "Cancel" }
                    onClick { pageNavigator.goBack() }
                }
                expanding.important.button {
                    text { content = "Next" }
                    action = Action("Next") {
                        val seasons = buildList {
                            if (winterEnabled.value) add(buildSeason("Winter", winterStartMonth, winterStartDay, winterEndMonth, winterEndDay))
                            if (springEnabled.value) add(buildSeason("Spring", springStartMonth, springStartDay, springEndMonth, springEndDay))
                            if (summerEnabled.value) add(buildSeason("Summer", summerStartMonth, summerStartDay, summerEndMonth, summerEndDay))
                            if (fallEnabled.value) add(buildSeason("Fall", fallStartMonth, fallStartDay, fallEndMonth, fallEndDay))
                        }
                        if (seasons.isEmpty()) throw PlainTextException("Please enable at least one season.", "Validation Error")

                        val trigger = TriggerBySeason(seasons = seasons)
                        val encoded = Json.encodeToString<com.dynamicwallpaper.Trigger>(trigger)
                        pageNavigator.navigate(SelectActionPage(playlistId, encoded))
                    }
                }
            }
        }
    }

    private fun buildSeason(
        name: String,
        startMonth: Signal<Int>, startDay: Signal<String>,
        endMonth: Signal<Int>, endDay: Signal<String>,
    ): Season {
        val sd = startDay.value.toIntOrNull() ?: 1
        val ed = endDay.value.toIntOrNull() ?: 1
        return Season(name = name, startMonth = startMonth.value, startDay = sd, endMonth = endMonth.value, endDay = ed)
    }

    private fun ViewWriter.seasonCard(
        name: String,
        enabled: Signal<Boolean>,
        startMonth: Signal<Int>, startDay: Signal<String>,
        endMonth: Signal<Int>, endDay: Signal<String>,
    ) {
        card.col {
            row {
                expanding.h3 { content = name }
                switch { checked bind enabled }
            }

            col {
                ::shown { enabled() }
                space()
                row {
                    expanding.field("Start Month") {
                        fieldTheme.select {
                            bind(startMonth, Signal((0..11).toList())) { monthNames[it] }
                        }
                    }
                    expanding.field("Start Day") {
                        fieldTheme.textInput {
                            hint = "1-31"
                            keyboardHints = KeyboardHints.integer
                            content bind startDay
                        }
                    }
                }
                space()
                row {
                    expanding.field("End Month") {
                        fieldTheme.select {
                            bind(endMonth, Signal((0..11).toList())) { monthNames[it] }
                        }
                    }
                    expanding.field("End Day") {
                        fieldTheme.textInput {
                            hint = "1-31"
                            keyboardHints = KeyboardHints.integer
                            content bind endDay
                        }
                    }
                }
            }
        }
    }
}
