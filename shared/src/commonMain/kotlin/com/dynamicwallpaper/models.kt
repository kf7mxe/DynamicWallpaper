package com.dynamicwallpaper

import com.lightningkite.EmailAddress
import com.lightningkite.services.data.*
import com.lightningkite.services.database.HasId
import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

// ── Enums ──

@Serializable
enum class AppPlatform {
    iOS, Android, Web, Desktop;
    companion object
}

@Serializable
enum class UserRole {
    NoOne, User, Creator, Admin, Root
}

@Serializable
enum class IntervalType {
    Minutes, Hour, Day, Week, Month
}

@Serializable
enum class WeatherCondition {
    Clear, MostlyClear, Clouds, MostlyClouds, Rain, LightRain, Snow, LightSnow,
    MostlySunny, Sunny, RainAndSnow, Thunderstorm, Fog, Windy
}

@Serializable
enum class WeatherLocationType {
    IpAddress, CurrentLocation, SpecificLocation
}

@Serializable
enum class ForecastUpdateInterval {
    Hourly, SixHours, TwelveHours, TwentyFourHours, TwoDays
}

@Serializable
enum class GeofenceTransition {
    Enter, Exit
}

// ── Core Models ──

@GenerateDataClassPaths
@Serializable
data class AppRelease(
    override val _id: Uuid = Uuid.random(),
    val version: String,
    val platform: AppPlatform,
    val releaseDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val requiredUpdate: Boolean,
) : HasId<Uuid>

@GenerateDataClassPaths
@Serializable
data class User(
    override val _id: Uuid = Uuid.random(),
    val email: EmailAddress,
    val name: String = "",
    val role: UserRole = UserRole.User,
    val createdAt: Instant = Clock.System.now(),
) : HasId<Uuid>

// ── Trigger Types (sealed hierarchy) ──

@Serializable
sealed class Trigger {
    abstract val displayName: String
    abstract val displayDescription: String
}

@Serializable
data class TriggerByTimeInterval(
    val intervalAmount: Int,
    val intervalType: IntervalType,
    val timeToTrigger: String = "00:00", // HH:mm format
    val dayOfWeek: String = "none", // comma-separated day names or "none"
    val isExact: Boolean = false,
) : Trigger() {
    override val displayName: String get() = "Time Interval"
    override val displayDescription: String
        get() = "Every $intervalAmount ${intervalType.name.lowercase()}(s) starting at $timeToTrigger"
}

@Serializable
data class TriggerByDate(
    val month: Int, // 0-based (Calendar style): 0=Jan, 11=Dec
    val day: Int,
) : Trigger() {
    override val displayName: String get() = "Specific Date"
    override val displayDescription: String get() {
        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        return "On ${monthNames.getOrElse(month) { "?" }} $day"
    }
}

@Serializable
data class Season(
    val name: String,
    val startMonth: Int,
    val startDay: Int,
    val endMonth: Int,
    val endDay: Int,
)

@Serializable
data class TriggerBySeason(
    val seasons: List<Season>,
) : Trigger() {
    override val displayName: String get() = "By Season"
    override val displayDescription: String
        get() = seasons.joinToString(", ") { it.name }
}

@Serializable
data class TriggerByLocation(
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Double,
    val transition: GeofenceTransition = GeofenceTransition.Enter,
) : Trigger() {
    override val displayName: String get() = "Location"
    override val displayDescription: String
        get() {
            val lat = ((latitude * 10000).toLong() / 10000.0).toString()
            val lon = ((longitude * 10000).toLong() / 10000.0).toString()
            return "${transition.name} geofence at ($lat, $lon)"
        }
}

@Serializable
data class TriggerByWeather(
    val temperatureIs: Int? = null,
    val temperatureLessThan: Int? = null,
    val temperatureGreaterThan: Int? = null,
    val temperatureBetweenLow: Int? = null,
    val temperatureBetweenHigh: Int? = null,
    val weatherCondition: WeatherCondition? = null,
    val locationType: WeatherLocationType = WeatherLocationType.IpAddress,
    val specificLatitude: Double? = null,
    val specificLongitude: Double? = null,
    val updateInterval: ForecastUpdateInterval = ForecastUpdateInterval.SixHours,
) : Trigger() {
    override val displayName: String get() = "Weather"
    override val displayDescription: String get() = buildString {
        temperatureIs?.let { append("Temp = $it°F") }
        temperatureLessThan?.let { append("Temp < $it°F") }
        temperatureGreaterThan?.let { append("Temp > $it°F") }
        if (temperatureBetweenLow != null && temperatureBetweenHigh != null) {
            append("Temp $temperatureBetweenLow-$temperatureBetweenHigh°F")
        }
        weatherCondition?.let { append("Condition: ${it.name}") }
    }
}

// ── Action Types (sealed hierarchy) ──

@Serializable
sealed class Action {
    abstract val displayName: String
    abstract val displayDescription: String
}

@Serializable
data object NextInPlaylist : Action() {
    override val displayName: String get() = "Next Wallpaper"
    override val displayDescription: String get() = "Go to next wallpaper in playlist"
}

@Serializable
data object PreviousInPlaylist : Action() {
    override val displayName: String get() = "Previous Wallpaper"
    override val displayDescription: String get() = "Go to previous wallpaper in playlist"
}

@Serializable
data object RandomInPlaylist : Action() {
    override val displayName: String get() = "Random Wallpaper"
    override val displayDescription: String get() = "Pick a random wallpaper"
}

@Serializable
data class SwitchToSubPlaylist(
    val subPlaylistName: String,
) : Action() {
    override val displayName: String get() = "Switch Sub-Playlist"
    override val displayDescription: String get() = "Switch to: $subPlaylistName"
}

@Serializable
data class SpecificWallpaper(
    val imageFileName: String,
) : Action() {
    override val displayName: String get() = "Specific Wallpaper"
    override val displayDescription: String get() = "Set: $imageFileName"
}

// ── Rule ──

@Serializable
data class Rule(
    val trigger: Trigger,
    val action: Action,
)

// ── SubPlaylist ──

@Serializable
data class SubPlaylist(
    val name: String,
    val fileNames: List<String> = emptyList(),
)

// ── Playlist (main entity) ──

@GenerateDataClassPaths
@Serializable
data class Playlist(
    override val _id: Uuid = Uuid.random(),
    val name: String = "",
    val ownerId: Uuid? = null,
    val photoFileNames: List<String> = emptyList(),
    val selectedImageIndex: Int = 0,
    val subPlaylists: List<SubPlaylist> = emptyList(),
    val selectedSubPlaylistIndex: Int = -1, // -1 = use top-level photos
    val subPlaylistSelectedImageIndex: Int = 0,
    val rules: List<Rule> = emptyList(),
    val isPublic: Boolean = false,
    val description: String = "",
    val downloadCount: Int = 0,
    val tags: List<String> = emptyList(),
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now(),
) : HasId<Uuid>

// ── Pack (image group for marketplace) ──

@GenerateDataClassPaths
@Serializable
data class Pack(
    override val _id: Uuid = Uuid.random(),
    val name: String = "",
    val description: String = "",
    val creatorId: Uuid? = null,
    val imageFileNames: List<String> = emptyList(),
    val previewImageUrls: List<String> = emptyList(),
    val downloadCount: Int = 0,
    val tags: List<String> = emptyList(),
    val isFeatured: Boolean = false,
    val isFree: Boolean = true,
    val createdAt: Instant = Clock.System.now(),
) : HasId<Uuid>
