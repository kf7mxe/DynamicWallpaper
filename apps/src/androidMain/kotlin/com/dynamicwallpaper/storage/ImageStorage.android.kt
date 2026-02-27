package com.dynamicwallpaper.storage

import android.net.Uri
import com.lightningkite.kiteui.FileReference
import com.lightningkite.kiteui.models.ImageLocal
import com.lightningkite.kiteui.models.ImageSource
import com.lightningkite.kiteui.toByteArray
import java.io.File

private fun imageDir(playlistId: String): File {
    val dir = File(AndroidContext.appContext.filesDir, "images/$playlistId")
    dir.mkdirs()
    return dir
}

private fun imageFile(playlistId: String, imageId: String): File {
    return File(imageDir(playlistId), "$imageId.jpg")
}

actual suspend fun saveImage(playlistId: String, imageId: String, file: FileReference) {
    val context = AndroidContext.appContext
    val uri = file.uri
    val inputStream = context.contentResolver.openInputStream(uri) ?: return
    val outFile = imageFile(playlistId, imageId)
    inputStream.use { input ->
        outFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }
}

actual suspend fun loadImageSource(playlistId: String, imageId: String): ImageSource? {
    val file = imageFile(playlistId, imageId)
    if (!file.exists()) return null
    return ImageLocal(FileReference(Uri.fromFile(file)))
}

actual fun deleteImage(playlistId: String, imageId: String) {
    imageFile(playlistId, imageId).delete()
}

actual suspend fun saveImageFromBlob(playlistId: String, imageId: String, blob: com.lightningkite.kiteui.Blob) {
    val bytes = blob.toByteArray()
    val outFile = imageFile(playlistId, imageId)
    outFile.writeBytes(bytes)
}

actual fun deletePlaylistImages(playlistId: String) {
    val dir = imageDir(playlistId)
    dir.deleteRecursively()
}
