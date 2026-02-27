package com.dynamicwallpaper.theming

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.Image
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

actual suspend fun loadImagePixels(playlistId: String, imageId: String, maxSize: Int): ImagePixels? {
    val storageKey = "image_${playlistId}_${imageId}"
    val dataUrl = window.localStorage.getItem(storageKey) ?: return null

    return suspendCoroutine { continuation ->
        val img = Image()

        img.onload = {
            try {
                var width = img.width
                var height = img.height

                if (width > maxSize || height > maxSize) {
                    val aspectRatio = width.toDouble() / height.toDouble()
                    if (width > height) {
                        width = maxSize
                        height = (maxSize / aspectRatio).toInt()
                    } else {
                        height = maxSize
                        width = (maxSize * aspectRatio).toInt()
                    }
                }

                val canvas = document.createElement("canvas") as HTMLCanvasElement
                canvas.width = width
                canvas.height = height

                val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
                ctx.drawImage(img, 0.0, 0.0, width.toDouble(), height.toDouble())

                val imageData = ctx.getImageData(0.0, 0.0, width.toDouble(), height.toDouble())
                val data = imageData.data.asDynamic()

                val pixels = FloatArray(width * height * 3)
                for (i in 0 until width * height) {
                    pixels[i * 3] = (data[i * 4] as Int).toFloat() / 255f
                    pixels[i * 3 + 1] = (data[i * 4 + 1] as Int).toFloat() / 255f
                    pixels[i * 3 + 2] = (data[i * 4 + 2] as Int).toFloat() / 255f
                }
                continuation.resume(ImagePixels(pixels, width, height))
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }

        img.onerror = { _, _, _, _, _ ->
            continuation.resumeWithException(Exception("Failed to load image for pixel extraction"))
        }

        img.src = dataUrl
    }
}
