package com.dynamicwallpaper.pages

import com.dynamicwallpaper.ForecastUpdateInterval
import com.dynamicwallpaper.TriggerByWeather
import com.dynamicwallpaper.WeatherCondition
import com.dynamicwallpaper.WeatherLocationType
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

@Routable("/playlists/{playlistId}/rules/trigger/weather")
class ConfigureWeatherTriggerPage(val playlistId: String) : Page {

    private enum class TempMode { None, Exact, LessThan, GreaterThan, Between }

    private val tempMode = Signal(TempMode.None)
    val tempValue = Signal("")
    val tempLow = Signal("")
    val tempHigh = Signal("")
    val weatherCondition = Signal<WeatherCondition?>(null)
    val useCondition = Signal(false)
    val locationType = Signal(WeatherLocationType.IpAddress)
    val specificLat = Signal("")
    val specificLon = Signal("")
    val updateInterval = Signal(ForecastUpdateInterval.SixHours)

    override fun ViewWriter.render() {
        scrolling.col {
            h2 { content = "Weather Trigger" }
            subtext { content = "Change wallpaper based on weather conditions." }

            space()

            // Temperature section
            card.col {
                h3 { content = "Temperature Condition" }
                space()
                tempModeButton("No temperature filter", TempMode.None)
                tempModeButton("Temperature is exactly", TempMode.Exact)
                tempModeButton("Temperature less than", TempMode.LessThan)
                tempModeButton("Temperature greater than", TempMode.GreaterThan)
                tempModeButton("Temperature between", TempMode.Between)

                // Single value input
                col {
                    ::shown { tempMode() in listOf(TempMode.Exact, TempMode.LessThan, TempMode.GreaterThan) }
                    space()
                    field("Temperature (°F)") {
                        fieldTheme.textInput {
                            hint = "e.g. 72"
                            keyboardHints = KeyboardHints.integer
                            content bind tempValue
                        }
                    }
                }

                // Range inputs
                col {
                    ::shown { tempMode() == TempMode.Between }
                    space()
                    row {
                        expanding.field("Low (°F)") {
                            fieldTheme.textInput {
                                hint = "e.g. 60"
                                keyboardHints = KeyboardHints.integer
                                content bind tempLow
                            }
                        }
                        expanding.field("High (°F)") {
                            fieldTheme.textInput {
                                hint = "e.g. 80"
                                keyboardHints = KeyboardHints.integer
                                content bind tempHigh
                            }
                        }
                    }
                }
            }

            space()

            // Weather condition section
            card.col {
                row {
                    expanding.h3 { content = "Weather Condition" }
                    switch { checked bind useCondition }
                }
                col {
                    ::shown { useCondition() }
                    space()
                    field("Condition") {
                        fieldTheme.select {
                            bind(weatherCondition, Signal(listOf(null) + WeatherCondition.entries)) {
                                it?.name ?: "Select..."
                            }
                        }
                    }
                }
            }

            space()

            // Location method
            card.col {
                h3 { content = "Location Method" }
                space()
                locationButton("IP Address", "Use approximate location from IP", WeatherLocationType.IpAddress)
                locationButton("Current Location", "Use device GPS", WeatherLocationType.CurrentLocation)
                locationButton("Specific Location", "Enter coordinates manually", WeatherLocationType.SpecificLocation)

                col {
                    ::shown { locationType() == WeatherLocationType.SpecificLocation }
                    space()
                    field("Latitude") {
                        fieldTheme.textInput {
                            hint = "e.g. 40.7128"
                            keyboardHints = KeyboardHints.decimal
                            content bind specificLat
                        }
                    }
                    space()
                    field("Longitude") {
                        fieldTheme.textInput {
                            hint = "e.g. -74.0060"
                            keyboardHints = KeyboardHints.decimal
                            content bind specificLon
                        }
                    }
                }
            }

            space()

            // Update interval
            field("Forecast Update Interval") {
                fieldTheme.select {
                    bind(updateInterval, Signal(ForecastUpdateInterval.entries.toList())) {
                        when (it) {
                            ForecastUpdateInterval.Hourly -> "Every hour"
                            ForecastUpdateInterval.SixHours -> "Every 6 hours"
                            ForecastUpdateInterval.TwelveHours -> "Every 12 hours"
                            ForecastUpdateInterval.TwentyFourHours -> "Every 24 hours"
                            ForecastUpdateInterval.TwoDays -> "Every 2 days"
                        }
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
                        // Validate
                        val hasTemp = tempMode.value != TempMode.None
                        val hasCond = useCondition.value && weatherCondition.value != null
                        if (!hasTemp && !hasCond) {
                            throw PlainTextException("Please set at least a temperature condition or weather condition.", "Validation Error")
                        }

                        val tempIs = if (tempMode.value == TempMode.Exact) tempValue.value.toIntOrNull() else null
                        val tempLt = if (tempMode.value == TempMode.LessThan) tempValue.value.toIntOrNull() else null
                        val tempGt = if (tempMode.value == TempMode.GreaterThan) tempValue.value.toIntOrNull() else null
                        val betweenLow = if (tempMode.value == TempMode.Between) tempLow.value.toIntOrNull() else null
                        val betweenHigh = if (tempMode.value == TempMode.Between) tempHigh.value.toIntOrNull() else null

                        if (hasTemp && tempMode.value != TempMode.Between) {
                            if (tempValue.value.toIntOrNull() == null) {
                                throw PlainTextException("Please enter a valid temperature.", "Validation Error")
                            }
                        }
                        if (tempMode.value == TempMode.Between && (betweenLow == null || betweenHigh == null)) {
                            throw PlainTextException("Please enter both low and high temperatures.", "Validation Error")
                        }

                        val specLat = if (locationType.value == WeatherLocationType.SpecificLocation)
                            specificLat.value.toDoubleOrNull() else null
                        val specLon = if (locationType.value == WeatherLocationType.SpecificLocation)
                            specificLon.value.toDoubleOrNull() else null

                        if (locationType.value == WeatherLocationType.SpecificLocation && (specLat == null || specLon == null)) {
                            throw PlainTextException("Please enter valid coordinates.", "Validation Error")
                        }

                        val trigger = TriggerByWeather(
                            temperatureIs = tempIs,
                            temperatureLessThan = tempLt,
                            temperatureGreaterThan = tempGt,
                            temperatureBetweenLow = betweenLow,
                            temperatureBetweenHigh = betweenHigh,
                            weatherCondition = if (hasCond) weatherCondition.value else null,
                            locationType = locationType.value,
                            specificLatitude = specLat,
                            specificLongitude = specLon,
                            updateInterval = updateInterval.value,
                        )
                        val encoded = Json.encodeToString<com.dynamicwallpaper.Trigger>(trigger)
                        pageNavigator.navigate(SelectActionPage(playlistId, encoded))
                    }
                }
            }
        }
    }

    private fun ViewWriter.tempModeButton(label: String, mode: TempMode) {
        button {
            row {
                expanding.text { content = label }
                col {
                    ::shown { tempMode() == mode }
                    icon(Icon.done, "Selected")
                }
            }
            onClick { tempMode.value = mode }
        }
    }

    private fun ViewWriter.locationButton(label: String, description: String, type: WeatherLocationType) {
        button {
            row {
                expanding.col {
                    text { content = label }
                    subtext { content = description }
                }
                col {
                    ::shown { locationType() == type }
                    icon(Icon.done, "Selected")
                }
            }
            onClick { locationType.value = type }
        }
    }
}
