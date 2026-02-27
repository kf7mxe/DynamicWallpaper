package com.dynamicwallpaper.engine

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.dynamicwallpaper.storage.AndroidContext
import kotlin.uuid.Uuid

object AlarmScheduler {

    private const val REQUEST_CODE_BASE = 10000

    fun schedule(
        playlistId: Uuid,
        ruleIndex: Int,
        triggerTimeMillis: Long,
        isExact: Boolean,
    ) {
        val context = AndroidContext.appContext
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmActionReceiver::class.java).apply {
            putExtra(AlarmActionReceiver.EXTRA_PLAYLIST_ID, playlistId.toString())
            putExtra(AlarmActionReceiver.EXTRA_RULE_INDEX, ruleIndex)
        }
        val requestCode = REQUEST_CODE_BASE + ruleIndex
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (isExact) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent
                    )
                } else {
                    // Fall back to inexact if permission not granted
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent
                )
            }
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent
            )
        }
    }

    fun cancel(ruleIndex: Int) {
        val context = AndroidContext.appContext
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmActionReceiver::class.java)
        val requestCode = REQUEST_CODE_BASE + ruleIndex
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun cancelAll(ruleCount: Int) {
        for (i in 0 until ruleCount) {
            cancel(i)
        }
    }
}
