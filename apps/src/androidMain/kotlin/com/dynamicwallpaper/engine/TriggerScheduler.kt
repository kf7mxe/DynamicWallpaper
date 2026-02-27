package com.dynamicwallpaper.engine

import com.dynamicwallpaper.*
import com.dynamicwallpaper.storage.LocalPlaylistStore
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.*
import kotlin.uuid.Uuid

object TriggerScheduler {

    fun scheduleAll() {
        val activeId = LocalPlaylistStore.activePlaylistId.value ?: return
        val playlist = LocalPlaylistStore.getById(activeId) ?: return

        // Cancel any existing alarms
        AlarmScheduler.cancelAll(playlist.rules.size)

        // Schedule each rule
        playlist.rules.forEachIndexed { index, rule ->
            scheduleRule(activeId, index, rule)
        }

        // Register geofences for location-based triggers
        GeofenceManager.registerForPlaylist(playlist)
    }

    fun scheduleRule(playlistId: Uuid, ruleIndex: Int, rule: Rule) {
        val triggerTime = calculateNextTriggerTime(rule.trigger) ?: return
        val isExact = (rule.trigger as? TriggerByTimeInterval)?.isExact ?: false
        AlarmScheduler.schedule(playlistId, ruleIndex, triggerTime, isExact)
    }

    private fun calculateNextTriggerTime(trigger: Trigger): Long? {
        val now = Clock.System.now()
        val tz = TimeZone.currentSystemDefault()
        val nowLocal = now.toLocalDateTime(tz)

        return when (trigger) {
            is TriggerByTimeInterval -> calculateIntervalTrigger(trigger, now, nowLocal, tz)
            is TriggerByDate -> calculateDateTrigger(trigger, nowLocal, tz)
            is TriggerBySeason -> calculateSeasonTrigger(trigger, nowLocal, tz)
            is TriggerByLocation -> null // Location triggers use geofencing, not alarms
            is TriggerByWeather -> calculateWeatherCheckTime(trigger, now)
        }
    }

    private fun calculateIntervalTrigger(
        trigger: TriggerByTimeInterval,
        now: Instant,
        nowLocal: LocalDateTime,
        tz: TimeZone,
    ): Long {
        val intervalMillis = when (trigger.intervalType) {
            IntervalType.Minutes -> trigger.intervalAmount * 60_000L
            IntervalType.Hour -> trigger.intervalAmount * 3_600_000L
            IntervalType.Day -> trigger.intervalAmount * 86_400_000L
            IntervalType.Week -> trigger.intervalAmount * 604_800_000L
            IntervalType.Month -> trigger.intervalAmount * 30L * 86_400_000L
        }

        // Parse start time
        val parts = trigger.timeToTrigger.split(":")
        val startHour = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val startMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        // Calculate next trigger from start time + interval
        val todayStart = LocalDateTime(nowLocal.date, LocalTime(startHour, startMinute))
        val todayStartInstant = todayStart.toInstant(tz)

        return if (todayStartInstant > now) {
            todayStartInstant.toEpochMilliseconds()
        } else {
            // Find next interval occurrence after now
            val elapsed = now.toEpochMilliseconds() - todayStartInstant.toEpochMilliseconds()
            val intervalsPassed = elapsed / intervalMillis + 1
            todayStartInstant.toEpochMilliseconds() + intervalsPassed * intervalMillis
        }
    }

    private fun calculateDateTrigger(
        trigger: TriggerByDate,
        nowLocal: LocalDateTime,
        tz: TimeZone,
    ): Long {
        val month = trigger.month + 1 // Convert 0-based to 1-based
        val targetThisYear = try {
            LocalDateTime(nowLocal.year, month, trigger.day, 0, 0)
        } catch (e: Exception) {
            return Long.MAX_VALUE
        }

        val targetInstant = if (targetThisYear.toInstant(tz) > Clock.System.now()) {
            targetThisYear.toInstant(tz)
        } else {
            // Schedule for next year
            try {
                LocalDateTime(nowLocal.year + 1, month, trigger.day, 0, 0).toInstant(tz)
            } catch (e: Exception) {
                return Long.MAX_VALUE
            }
        }
        return targetInstant.toEpochMilliseconds()
    }

    private fun calculateSeasonTrigger(
        trigger: TriggerBySeason,
        nowLocal: LocalDateTime,
        tz: TimeZone,
    ): Long? {
        // Find the next season start date
        val candidates = trigger.seasons.flatMap { season ->
            listOf(
                seasonStartDateTime(season, nowLocal.year, tz),
                seasonStartDateTime(season, nowLocal.year + 1, tz),
            )
        }
        val now = Clock.System.now().toEpochMilliseconds()
        return candidates
            .filter { it > now }
            .minOrNull()
    }

    private fun seasonStartDateTime(season: Season, year: Int, tz: TimeZone): Long {
        return try {
            val dt = LocalDateTime(year, season.startMonth + 1, season.startDay, 0, 0)
            dt.toInstant(tz).toEpochMilliseconds()
        } catch (e: Exception) {
            Long.MAX_VALUE
        }
    }

    private fun calculateWeatherCheckTime(
        trigger: TriggerByWeather,
        now: Instant,
    ): Long {
        val intervalMillis = when (trigger.updateInterval) {
            ForecastUpdateInterval.Hourly -> 3_600_000L
            ForecastUpdateInterval.SixHours -> 6 * 3_600_000L
            ForecastUpdateInterval.TwelveHours -> 12 * 3_600_000L
            ForecastUpdateInterval.TwentyFourHours -> 24 * 3_600_000L
            ForecastUpdateInterval.TwoDays -> 48 * 3_600_000L
        }
        return now.toEpochMilliseconds() + intervalMillis
    }
}
