package com.kf7mxe.autowall

import com.lightningkite.lightningserver.media.ServerFileWithMetadata
import com.lightningkite.services.data.*
import com.lightningkite.services.database.HasId
import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Clock.System.now
import kotlin.time.Instant
import kotlin.uuid.Uuid

// ── Enums ──



const val databaseVersion = 1

@Serializable
data class ModelTableVersionContainer(val version: Int = databaseVersion, val table: String)


@Serializable
enum class UserRole {
    USER, TESTER, ADMIN, ROOT
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
//    val platform: AppPlatform,
    val releaseDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val requiredUpdate: Boolean,
) : HasId<Uuid>

@GenerateDataClassPaths
@Serializable
data class User(
    override val _id: Uuid = Uuid.random(),
    val email: EmailAddress,
    val name: String = "",
    val role: UserRole = UserRole.USER,
    val createdAt: Instant = Clock.System.now(),
    val primaryDeviceId: String? = null,
) : HasId<Uuid>


interface MaybeHasUser<ID : Comparable<ID>> {
    val user: ID?
}

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
sealed class PlaylistAction {
    abstract val displayName: String
    abstract val displayDescription: String
}

@Serializable
data object NextInPlaylistAction : PlaylistAction() {
    override val displayName: String get() = "Next Wallpaper"
    override val displayDescription: String get() = "Go to next wallpaper in playlist"
}

@Serializable
data object PreviousInPlaylistAction : PlaylistAction() {
    override val displayName: String get() = "Previous Wallpaper"
    override val displayDescription: String get() = "Go to previous wallpaper in playlist"
}

@Serializable
data object RandomInPlaylistAction : PlaylistAction() {
    override val displayName: String get() = "Random Wallpaper"
    override val displayDescription: String get() = "Pick a random wallpaper"
}

@Serializable
data class SwitchToSubPlaylistAction(
    val subPlaylistUuid: Uuid,
    @Denormalized val subPlaylistName: String? = null,
) : PlaylistAction() {
    override val displayName: String get() = "Switch Sub-Playlist"
    override val displayDescription: String get() = "Switch to: $subPlaylistName"
}

@Serializable
data class SpecificWallpaperAction(
    val wallpaperId: Uuid,
    @Denormalized val wallpaperName: String,
) : PlaylistAction() {
    override val displayName: String get() = "Specific Wallpaper"
    override val displayDescription: String get() = "Set: ${wallpaperName}"
}

// ── Rule ──

@Serializable
data class Rule(
    val trigger: Trigger,
    val action: PlaylistAction,
)

// ── SubPlaylist ──

// ── Playlist (main entity) ──

@GenerateDataClassPaths
@Serializable
data class SubPlaylist(
    override val _id: Uuid = Uuid.random(), // Add the ID propert
    val name: String,
    @References(User::class) override val user: Uuid? = null,
    val wallpapers: List<Uuid> = emptyList(),
): HasId<Uuid>, MaybeHasUser<Uuid>
@GenerateDataClassPaths
@Serializable
data class Playlist(
    override val _id: Uuid = Uuid.random(),
    val name: String,
    @References(User::class) override val user: Uuid? = null,
    val wallpapers: List<Uuid> = emptyList(),
    val currentImageIndex: Int = 0,
    val subPlaylists: List<Uuid> = emptyList(),
    val currentSubPlaylistIndex: Int? = null, // -1 = use top-level photos
    val currentSubPlaylistWallpaperIndex: Int? = null,
    val rules: List<Rule> = emptyList(),
    val description: String = "",
    val tags: List<String> = emptyList(),
    val createdAt: Instant = now(),
    val updatedAt: Instant = now(),
) : HasId<Uuid>, MaybeHasUser<Uuid>



@GenerateDataClassPaths
@Serializable
data class PlaylistTemplate(
    override val _id: Uuid = Uuid.random(),
    val name: String,
    @References(User::class) val uploadedBy: Uuid? = null,
    val wallpapers: List<Uuid> = emptyList(),
    val subPlaylists: List<Uuid> = emptyList(),
    val rules: List<Rule> = emptyList(),
    val description: String = "",
    val downloadCount: Int = 0,
    val tags: List<String> = emptyList(),
    val createdAt: Instant = now(),
    val updatedAt: Instant = now(),
) : HasId<Uuid>



@GenerateDataClassPaths
@Serializable
data class Wallpaper(
    override val _id: Uuid = Uuid.random(),
    @References(User::class) val user: Uuid? = null,
    val name:String,
    val remoteFile: ServerFileWithMetadata,
    val localFileReference: String,
) : HasId<Uuid>

// ── Pack (image group for marketplace) ──

@GenerateDataClassPaths
@Serializable
data class StoreWallpaperPack(
    override val _id: Uuid = Uuid.random(),
    val name: String = "",
    val description: String = "",
    val creatorId: Uuid? = null,
    val wallpapers: List<Uuid> = emptyList(),
    val downloadCount: Int = 0,
    val tags: List<String> = emptyList(),
    val isFree: Boolean = true,
    val createdAt: Instant = now(),
) : HasId<Uuid>


@GenerateDataClassPaths
@Serializable
data class WallpaperPack(
    override val _id: Uuid = Uuid.random(),
    val name: String,
    val description: String = "",
    override val user: Uuid? = null,
    val wallpapers: List<Uuid> = emptyList(),
    val createdAt: Instant = now(),
    ) : HasId<Uuid>, MaybeHasUser<Uuid>


enum class StoreType {
    Apple,
    Google,
    Manual,
    Stripe
}


@Serializable
@GenerateDataClassPaths
data class Product(
    override val _id: String, // This is the ID of the Store Product. There is a 1 to 1 in our system
    val storeType: StoreType,
    val name: String,
    val htmlDescription: String?,
    val group: String? = null,
    val order: Int = 0,
    val archived: Boolean,
) : HasId<String>




@Serializable
@GenerateDataClassPaths
data class Subscription(
    override val _id: Uuid = Uuid.random(),
    @Index @References(User::class) val user: Uuid,
    @Index @AdminViewOnly val transactionId: String?,
    @Index val storeType: StoreType,
    @References(Product::class) val product: String,
    @AdminViewOnly val basePrice: String? = null, // Used By Android
    @Index val expires: kotlinx.datetime.Instant,
    @Index val startTime: kotlinx.datetime.Instant,
    @AdminHidden val autoRenewing: Boolean = false,
    @References(Product::class) @AdminHidden val renewChange: String? = null,
) : HasId<Uuid>


@Serializable
enum class GoogleIAPEnvironment {
    Live,
    Mock,
}

@Serializable
enum class AppleIAPEnvironment {
    Live,
    Mock,
}

@Serializable
data class GoogleReceiptValidationRequest(
    val orderId: String,
    val packageName: String,
    val productIds: List<String>,
    val purchaseToken: String,
    val autoRenewing: Boolean,
)

@Serializable
data class GoogleSubscriptionReceiptValidationResult(
    val expires: Instant,
    val renews: Boolean,
    val productId: String,
    val baseId: String? = null,
    val transactionId: String,
)

@Serializable
data class GoogleProductPurchase(
    val purchaseTimeMillis: String? = null,
    val purchaseState: Int? = null, // 0 = Purchased, 1 = Canceled, 2 = Pending
    val consumptionState: Int? = null, // 0 = Not consumed, 1 = Consumed
    val orderId: String? = null,
    val acknowledgementState: Int? = null // 0 = Yet to be acknowledged, 1 = Acknowledged
)

@Serializable
data class  PurchaseReceipt(
    val _id: Uuid = Uuid.random(),
    val userId: Uuid,
    val productId: Uuid,
    val store: StoreType, // "GOOGLE_PLAY" or "APP_STORE"
    val transactionId: String, // The receipt token
    val rawReceiptData: String, // Store the raw JSON just in case you need to debug
    val purchasedAt: Instant = Clock.System.now()
)