package com.dynamicwallpaper.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dynamicwallpaper.storage.AndroidContext

class BootCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        AndroidContext.appContext = context.applicationContext
        TriggerScheduler.scheduleAll()
    }
}
