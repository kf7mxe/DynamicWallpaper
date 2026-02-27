package com.dynamicwallpaper.pages

import com.dynamicwallpaper.IntervalType
import com.dynamicwallpaper.TriggerByTimeInterval
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

@Routable("/playlists/{playlistId}/rules/trigger/time-interval")
class ConfigureTimeIntervalPage(val playlistId: String) : Page {

    val intervalAmount = Signal("1")
    val intervalType = Signal(IntervalType.Hour)
    val timeToTrigger = Signal("00:00")
    val isExact = Signal(false)

    // Day of week toggles
    val monday = Signal(false)
    val tuesday = Signal(false)
    val wednesday = Signal(false)
    val thursday = Signal(false)
    val friday = Signal(false)
    val saturday = Signal(false)
    val sunday = Signal(false)

    override fun ViewWriter.render() {
        scrolling.col {
            h2 { content = "Time Interval Trigger" }
            subtext { content = "Change wallpaper at regular time intervals." }

            space()

            field("Interval Amount") {
                fieldTheme.textInput {
                    hint = "e.g. 5"
                    keyboardHints = KeyboardHints.integer
                    content bind intervalAmount
                }
            }

            space()

            field("Interval Type") {
                fieldTheme.select {
                    bind(intervalType, Signal(IntervalType.entries.toList())) { it.name }
                }
            }

            space()

            field("Start Time (HH:mm)") {
                fieldTheme.textInput {
                    hint = "00:00"
                    content bind timeToTrigger
                }
            }

            space()

            // Day of week selection - shown for Week interval type
            card.col {
                ::shown { intervalType() == IntervalType.Week }
                h3 { content = "Days of Week" }
                subtext { content = "Select which days to trigger." }
                space()
                dayToggle("Monday", monday)
                dayToggle("Tuesday", tuesday)
                dayToggle("Wednesday", wednesday)
                dayToggle("Thursday", thursday)
                dayToggle("Friday", friday)
                dayToggle("Saturday", saturday)
                dayToggle("Sunday", sunday)
            }

            space()

            row {
                expanding.col {
                    text { content = "Exact Timing" }
                    subtext { content = "Use exact alarms (may use more battery)" }
                }
                switch { checked bind isExact }
            }

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
                        val amount = intervalAmount.value.toIntOrNull()
                            ?: throw PlainTextException("Please enter a valid number.", "Validation Error")
                        if (amount <= 0) throw PlainTextException("Interval must be greater than 0.", "Validation Error")

                        val dayOfWeek = if (intervalType.value == IntervalType.Week) {
                            buildList {
                                if (monday.value) add("Monday")
                                if (tuesday.value) add("Tuesday")
                                if (wednesday.value) add("Wednesday")
                                if (thursday.value) add("Thursday")
                                if (friday.value) add("Friday")
                                if (saturday.value) add("Saturday")
                                if (sunday.value) add("Sunday")
                            }.joinToString(",").ifEmpty { "none" }
                        } else "none"

                        val trigger = TriggerByTimeInterval(
                            intervalAmount = amount,
                            intervalType = intervalType.value,
                            timeToTrigger = timeToTrigger.value,
                            dayOfWeek = dayOfWeek,
                            isExact = isExact.value,
                        )
                        val encoded = Json.encodeToString<com.dynamicwallpaper.Trigger>(trigger)
                        pageNavigator.navigate(SelectActionPage(playlistId, encoded))
                    }
                }
            }
        }
    }

    private fun ViewWriter.dayToggle(name: String, signal: Signal<Boolean>) {
        row {
            expanding.text { content = name }
            switch { checked bind signal }
        }
    }
}
