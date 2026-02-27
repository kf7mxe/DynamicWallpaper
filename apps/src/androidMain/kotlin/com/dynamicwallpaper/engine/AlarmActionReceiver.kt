package com.dynamicwallpaper.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dynamicwallpaper.storage.AndroidContext
import com.dynamicwallpaper.storage.LocalPlaylistStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class AlarmActionReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_PLAYLIST_ID = "playlist_id"
        const val EXTRA_RULE_INDEX = "rule_index"
    }

    override fun onReceive(context: Context, intent: Intent) {
        AndroidContext.appContext = context.applicationContext

        val playlistIdStr = intent.getStringExtra(EXTRA_PLAYLIST_ID) ?: return
        val ruleIndex = intent.getIntExtra(EXTRA_RULE_INDEX, -1)
        if (ruleIndex < 0) return

        val playlistId = try {
            Uuid.parse(playlistIdStr)
        } catch (e: Exception) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val playlist = LocalPlaylistStore.getById(playlistId) ?: return@launch
                val rule = playlist.rules.getOrNull(ruleIndex) ?: return@launch
                WallpaperActionRunner.executeAction(playlistId, rule.action)
                // Re-schedule this alarm for the next trigger time
                TriggerScheduler.scheduleRule(playlistId, ruleIndex, rule)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
