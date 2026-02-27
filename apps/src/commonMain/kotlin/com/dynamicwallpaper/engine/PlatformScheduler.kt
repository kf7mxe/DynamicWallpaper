package com.dynamicwallpaper.engine

/**
 * Platform-specific trigger scheduling.
 * On Android, this schedules AlarmManager alarms.
 * On other platforms, this is a no-op.
 */
expect fun scheduleTriggers()
