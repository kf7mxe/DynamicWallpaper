package com.dynamicwallpaper.pages

import com.dynamicwallpaper.TriggerByDate
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

private val monthNames = listOf("January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December")

@Routable("/playlists/{playlistId}/rules/trigger/date")
class ConfigureDateTriggerPage(val playlistId: String) : Page {

    val selectedMonth = Signal(0) // 0-based
    val dayInput = Signal("1")

    override fun ViewWriter.render() {
        scrolling.col {
            h2 { content = "Date Trigger" }
            subtext { content = "Change wallpaper on a specific date each year." }

            space()

            field("Month") {
                fieldTheme.select {
                    bind(selectedMonth, Signal((0..11).toList())) { monthNames[it] }
                }
            }

            space()

            field("Day of Month") {
                fieldTheme.textInput {
                    hint = "1-31"
                    keyboardHints = KeyboardHints.integer
                    content bind dayInput
                }
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
                        val day = dayInput.value.toIntOrNull()
                            ?: throw PlainTextException("Please enter a valid day.", "Validation Error")
                        if (day < 1 || day > 31) throw PlainTextException("Day must be between 1 and 31.", "Validation Error")

                        val trigger = TriggerByDate(
                            month = selectedMonth.value,
                            day = day,
                        )
                        val encoded = Json.encodeToString<com.dynamicwallpaper.Trigger>(trigger)
                        pageNavigator.navigate(SelectActionPage(playlistId, encoded))
                    }
                }
            }
        }
    }
}
