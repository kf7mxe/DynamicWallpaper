package com.dynamicwallpaper.engine

import com.dynamicwallpaper.TriggerByWeather
import com.dynamicwallpaper.WeatherCondition
import com.lightningkite.kiteui.fetch
import com.lightningkite.kiteui.httpHeaders
import kotlinx.serialization.json.*

data class WeatherData(
    val temperature: Int,
    val condition: WeatherCondition,
)

object WeatherService {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Fetch current weather for given coordinates using the NWS API.
     * NWS API is free and requires no API key (US locations only).
     * Flow: GET /points/{lat},{lon} → get forecastUrl → GET forecast → parse current period.
     */
    suspend fun getWeatherForLocation(lat: Double, lon: Double): WeatherData? {
        return try {
            // Step 1: Get the forecast URL for this location
            val pointsUrl = "https://api.weather.gov/points/${lat},${lon}"
            val pointsResponse = fetch(
                url = pointsUrl,
                headers = httpHeaders("Accept" to "application/geo+json", "User-Agent" to "DynamicWallpaperApp")
            )
            if (!pointsResponse.ok) return null

            val pointsJson = json.parseToJsonElement(pointsResponse.text()).jsonObject
            val forecastUrl = pointsJson["properties"]
                ?.jsonObject?.get("forecast")
                ?.jsonPrimitive?.content ?: return null

            // Step 2: Get the forecast
            val forecastResponse = fetch(
                url = forecastUrl,
                headers = httpHeaders("Accept" to "application/geo+json", "User-Agent" to "DynamicWallpaperApp")
            )
            if (!forecastResponse.ok) return null

            val forecastJson = json.parseToJsonElement(forecastResponse.text()).jsonObject
            val periods = forecastJson["properties"]
                ?.jsonObject?.get("periods")
                ?.jsonArray ?: return null

            val current = periods.firstOrNull()?.jsonObject ?: return null
            val temperature = current["temperature"]?.jsonPrimitive?.intOrNull ?: return null
            val shortForecast = current["shortForecast"]?.jsonPrimitive?.content ?: ""

            WeatherData(
                temperature = temperature,
                condition = parseWeatherCondition(shortForecast)
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if current weather matches a TriggerByWeather rule.
     */
    fun matchesTrigger(weather: WeatherData, trigger: TriggerByWeather): Boolean {
        // Check temperature conditions
        trigger.temperatureIs?.let { if (weather.temperature != it) return false }
        trigger.temperatureLessThan?.let { if (weather.temperature >= it) return false }
        trigger.temperatureGreaterThan?.let { if (weather.temperature <= it) return false }
        val low = trigger.temperatureBetweenLow
        val high = trigger.temperatureBetweenHigh
        if (low != null && high != null) {
            if (weather.temperature < low || weather.temperature > high) return false
        }
        // Check weather condition
        trigger.weatherCondition?.let { if (weather.condition != it) return false }
        return true
    }

    private fun parseWeatherCondition(forecast: String): WeatherCondition {
        val lower = forecast.lowercase()
        return when {
            "thunderstorm" in lower -> WeatherCondition.Thunderstorm
            "fog" in lower -> WeatherCondition.Fog
            "windy" in lower || "wind" in lower -> WeatherCondition.Windy
            "rain and snow" in lower || "wintry mix" in lower -> WeatherCondition.RainAndSnow
            "snow" in lower && "light" in lower -> WeatherCondition.LightSnow
            "snow" in lower -> WeatherCondition.Snow
            "rain" in lower && "light" in lower -> WeatherCondition.LightRain
            "rain" in lower || "showers" in lower -> WeatherCondition.Rain
            "mostly cloudy" in lower || "overcast" in lower -> WeatherCondition.MostlyClouds
            "partly cloudy" in lower || "cloud" in lower -> WeatherCondition.Clouds
            "mostly sunny" in lower -> WeatherCondition.MostlySunny
            "sunny" in lower -> WeatherCondition.Sunny
            "mostly clear" in lower -> WeatherCondition.MostlyClear
            "clear" in lower -> WeatherCondition.Clear
            else -> WeatherCondition.Clear
        }
    }
}
