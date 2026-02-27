package com.dynamicwallpaper.engine

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.dynamicwallpaper.GeofenceTransition
import com.dynamicwallpaper.Playlist
import com.dynamicwallpaper.TriggerByLocation
import com.dynamicwallpaper.storage.AndroidContext
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

object GeofenceManager {

    private const val TAG = "GeofenceManager"
    private const val GEOFENCE_PREFIX = "dw_geo_"

    fun registerForPlaylist(playlist: Playlist) {
        val context = AndroidContext.appContext

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Missing ACCESS_FINE_LOCATION permission, skipping geofence registration")
            return
        }

        val client = LocationServices.getGeofencingClient(context)

        // Collect geofences from location-based rules
        val geofences = mutableListOf<Geofence>()
        playlist.rules.forEachIndexed { index, rule ->
            val trigger = rule.trigger as? TriggerByLocation ?: return@forEachIndexed

            val transitionType = when (trigger.transition) {
                GeofenceTransition.Enter -> Geofence.GEOFENCE_TRANSITION_ENTER
                GeofenceTransition.Exit -> Geofence.GEOFENCE_TRANSITION_EXIT
            }

            geofences.add(
                Geofence.Builder()
                    .setRequestId("${GEOFENCE_PREFIX}${playlist._id}_$index")
                    .setCircularRegion(trigger.latitude, trigger.longitude, trigger.radiusMeters.toFloat())
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(transitionType)
                    .build()
            )
        }

        if (geofences.isEmpty()) return

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofences)
            .build()

        client.addGeofences(request, createPendingIntent())
            .addOnSuccessListener { Log.d(TAG, "Registered ${geofences.size} geofences") }
            .addOnFailureListener { Log.e(TAG, "Failed to register geofences", it) }
    }

    fun unregisterAll() {
        val context = AndroidContext.appContext
        val client = LocationServices.getGeofencingClient(context)
        client.removeGeofences(createPendingIntent())
            .addOnSuccessListener { Log.d(TAG, "Removed all geofences") }
            .addOnFailureListener { Log.e(TAG, "Failed to remove geofences", it) }
    }

    private fun createPendingIntent(): PendingIntent {
        val context = AndroidContext.appContext
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }
}
