package com.dynamicwallpaper.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dynamicwallpaper.storage.AndroidContext
import com.dynamicwallpaper.storage.LocalPlaylistStore
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "GeofenceReceiver"
        private const val GEOFENCE_PREFIX = "dw_geo_"
    }

    override fun onReceive(context: Context, intent: Intent) {
        AndroidContext.appContext = context.applicationContext

        val event = GeofencingEvent.fromIntent(intent)
        if (event == null) {
            Log.w(TAG, "Null geofencing event")
            return
        }
        if (event.hasError()) {
            Log.e(TAG, "Geofencing error: ${event.errorCode}")
            return
        }

        val triggeringGeofences = event.triggeringGeofences ?: return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                for (geofence in triggeringGeofences) {
                    val requestId = geofence.requestId
                    if (!requestId.startsWith(GEOFENCE_PREFIX)) continue

                    // Parse: "dw_geo_{playlistId}_{ruleIndex}"
                    val suffix = requestId.removePrefix(GEOFENCE_PREFIX)
                    val lastUnderscore = suffix.lastIndexOf('_')
                    if (lastUnderscore < 0) continue

                    val playlistIdStr = suffix.substring(0, lastUnderscore)
                    val ruleIndexStr = suffix.substring(lastUnderscore + 1)

                    val playlistId = try { Uuid.parse(playlistIdStr) } catch (e: Exception) { continue }
                    val ruleIndex = ruleIndexStr.toIntOrNull() ?: continue

                    val playlist = LocalPlaylistStore.getById(playlistId) ?: continue
                    val rule = playlist.rules.getOrNull(ruleIndex) ?: continue

                    Log.d(TAG, "Executing geofence action for playlist=$playlistId rule=$ruleIndex")
                    WallpaperActionRunner.executeAction(playlistId, rule.action)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
