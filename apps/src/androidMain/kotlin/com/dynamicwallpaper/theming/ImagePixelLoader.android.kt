package com.dynamicwallpaper.theming

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.dynamicwallpaper.storage.AndroidContext
import java.io.File

actual suspend fun loadImagePixels(playlistId: String, imageId: String, maxSize: Int): ImagePixels? {
    val file = File(AndroidContext.appContext.filesDir, "images/$playlistId/$imageId.jpg")
    if (!file.exists()) return null

    // Decode bounds only
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, options)

    // Calculate inSampleSize
    var inSampleSize = 1
    val halfHeight = options.outHeight / 2
    val halfWidth = options.outWidth / 2
    while ((halfHeight / inSampleSize) >= maxSize && (halfWidth / inSampleSize) >= maxSize) {
        inSampleSize *= 2
    }

    // Decode with inSampleSize
    options.inJustDecodeBounds = false
    options.inSampleSize = inSampleSize

    val sampledBitmap = BitmapFactory.decodeFile(file.absolutePath, options) ?: return null

    // Scale to exact max dimensions
    val width = sampledBitmap.width
    val height = sampledBitmap.height
    var finalWidth = width
    var finalHeight = height

    if (width > maxSize || height > maxSize) {
        val aspectRatio = width.toDouble() / height.toDouble()
        if (width > height) {
            finalWidth = maxSize
            finalHeight = (maxSize / aspectRatio).toInt()
        } else {
            finalHeight = maxSize
            finalWidth = (maxSize * aspectRatio).toInt()
        }
    }

    val finalBitmap = if (width != finalWidth || height != finalHeight) {
        val scaled = Bitmap.createScaledBitmap(sampledBitmap, finalWidth, finalHeight, true)
        if (scaled != sampledBitmap) sampledBitmap.recycle()
        scaled
    } else {
        sampledBitmap
    }

    val w = finalBitmap.width
    val h = finalBitmap.height
    val intPixels = IntArray(w * h)
    finalBitmap.getPixels(intPixels, 0, w, 0, 0, w, h)

    val floatPixels = FloatArray(w * h * 3)
    for (i in intPixels.indices) {
        val color = intPixels[i]
        floatPixels[i * 3] = android.graphics.Color.red(color) / 255f
        floatPixels[i * 3 + 1] = android.graphics.Color.green(color) / 255f
        floatPixels[i * 3 + 2] = android.graphics.Color.blue(color) / 255f
    }

    finalBitmap.recycle()

    return ImagePixels(floatPixels, w, h)
}
