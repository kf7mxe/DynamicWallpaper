package com.dynamicwallpaper.pages

import com.dynamicwallpaper.GeofenceTransition
import com.dynamicwallpaper.TriggerByLocation
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

@Routable("/playlists/{playlistId}/rules/trigger/location")
class ConfigureLocationTriggerPage(val playlistId: String) : Page {

    val latitude = Signal("")
    val longitude = Signal("")
    val radius = Signal("100")
    val transition = Signal(GeofenceTransition.Enter)

    override fun ViewWriter.render() {
        scrolling.col {
            h2 { content = "Location Trigger" }
            subtext { content = "Change wallpaper when entering or leaving a location." }

            space()

            field("Latitude") {
                fieldTheme.textInput {
                    hint = "e.g. 40.7128"
                    keyboardHints = KeyboardHints.decimal
                    content bind latitude
                }
            }

            space()

            field("Longitude") {
                fieldTheme.textInput {
                    hint = "e.g. -74.0060"
                    keyboardHints = KeyboardHints.decimal
                    content bind longitude
                }
            }

            space()

            field("Radius (meters)") {
                fieldTheme.textInput {
                    hint = "e.g. 100"
                    keyboardHints = KeyboardHints.integer
                    content bind radius
                }
            }

            space()

            card.col {
                h3 { content = "Transition" }
                subtext { content = "Trigger when you:" }
                space()
                row {
                    expanding.button {
                        row {
                            expanding.text { content = "Enter area" }
                            col {
                                ::shown { transition() == GeofenceTransition.Enter }
                                icon(Icon.done, "Selected")
                            }
                        }
                        onClick { transition.value = GeofenceTransition.Enter }
                    }
                    expanding.button {
                        row {
                            expanding.text { content = "Exit area" }
                            col {
                                ::shown { transition() == GeofenceTransition.Exit }
                                icon(Icon.done, "Selected")
                            }
                        }
                        onClick { transition.value = GeofenceTransition.Exit }
                    }
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
                        val lat = latitude.value.toDoubleOrNull()
                            ?: throw PlainTextException("Please enter a valid latitude.", "Validation Error")
                        val lon = longitude.value.toDoubleOrNull()
                            ?: throw PlainTextException("Please enter a valid longitude.", "Validation Error")
                        val rad = radius.value.toDoubleOrNull()
                            ?: throw PlainTextException("Please enter a valid radius.", "Validation Error")
                        if (rad <= 0) throw PlainTextException("Radius must be greater than 0.", "Validation Error")

                        val trigger = TriggerByLocation(
                            latitude = lat,
                            longitude = lon,
                            radiusMeters = rad,
                            transition = transition.value,
                        )
                        val encoded = Json.encodeToString<com.dynamicwallpaper.Trigger>(trigger)
                        pageNavigator.navigate(SelectActionPage(playlistId, encoded))
                    }
                }
            }
        }
    }
}
