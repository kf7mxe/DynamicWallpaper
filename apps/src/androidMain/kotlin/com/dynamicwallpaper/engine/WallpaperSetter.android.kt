package com.dynamicwallpaper.engine

import android.app.WallpaperManager
import android.graphics.BitmapFactory
import com.dynamicwallpaper.storage.AndroidContext
import java.io.File

actual suspend fun setWallpaper(playlistId: String, imageId: String) {
    val context = AndroidContext.appContext
    val file = File(context.filesDir, "images/$playlistId/$imageId.jpg")
    if (!file.exists()) return
    val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return
    try {
        val wm = WallpaperManager.getInstance(context)
        val which = when (WallpaperTargetManager.target.value) {
            WallpaperTarget.HomeScreen -> WallpaperManager.FLAG_SYSTEM
            WallpaperTarget.LockScreen -> WallpaperManager.FLAG_LOCK
            WallpaperTarget.Both -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
        }
        wm.setBitmap(bitmap, null, true, which)
    } finally {
        bitmap.recycle()
    }
}
