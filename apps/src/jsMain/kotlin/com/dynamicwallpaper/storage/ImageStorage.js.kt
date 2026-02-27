package com.dynamicwallpaper.storage

import com.lightningkite.kiteui.Blob
import com.lightningkite.kiteui.FileReference
import com.lightningkite.kiteui.models.ImageRaw
import com.lightningkite.kiteui.models.ImageSource
import kotlinx.browser.window
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise

private fun storageKey(playlistId: String, imageId: String): String =
    "image_${playlistId}_${imageId}"

private fun playlistPrefix(playlistId: String): String =
    "image_${playlistId}_"

actual suspend fun saveImage(playlistId: String, imageId: String, file: FileReference) {
    val blob = file.asDynamic() as org.w3c.files.Blob
    val base64 = suspendCoroutine<String> { cont ->
        val reader = FileReader()
        reader.onload = { _ ->
            cont.resume(reader.result as String)
        }
        reader.readAsDataURL(blob)
    }
    window.localStorage.setItem(storageKey(playlistId, imageId), base64)
}

actual suspend fun loadImageSource(playlistId: String, imageId: String): ImageSource? {
    val base64 = window.localStorage.getItem(storageKey(playlistId, imageId)) ?: return null
    // For JS, we store as data URL and can use ImageRemote with data URL
    return com.lightningkite.kiteui.models.ImageRemote(base64)
}

actual fun deleteImage(playlistId: String, imageId: String) {
    window.localStorage.removeItem(storageKey(playlistId, imageId))
}

actual suspend fun saveImageFromBlob(playlistId: String, imageId: String, blob: com.lightningkite.kiteui.Blob) {
    // Convert blob to base64 data URL for localStorage storage
    val jsBlob = blob.asDynamic() as org.w3c.files.Blob
    val base64 = suspendCoroutine<String> { cont ->
        val reader = FileReader()
        reader.onload = { _ ->
            cont.resume(reader.result as String)
        }
        reader.readAsDataURL(jsBlob)
    }
    window.localStorage.setItem(storageKey(playlistId, imageId), base64)
}

actual fun deletePlaylistImages(playlistId: String) {
    val prefix = playlistPrefix(playlistId)
    val keysToRemove = mutableListOf<String>()
    for (i in 0 until window.localStorage.length) {
        val key = window.localStorage.key(i) ?: continue
        if (key.startsWith(prefix)) {
            keysToRemove.add(key)
        }
    }
    keysToRemove.forEach { window.localStorage.removeItem(it) }
}
