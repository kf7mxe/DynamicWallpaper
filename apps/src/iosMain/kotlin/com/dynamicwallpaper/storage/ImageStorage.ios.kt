package com.dynamicwallpaper.storage

import com.lightningkite.kiteui.FileReference
import com.lightningkite.kiteui.models.ImageLocal
import com.lightningkite.kiteui.models.ImageSource
import com.lightningkite.kiteui.toByteArray
import platform.Foundation.*

private fun imageDir(playlistId: String): String {
    val paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
    val documentsDir = paths.firstOrNull() as? String ?: return ""
    val dir = "$documentsDir/images/$playlistId"
    NSFileManager.defaultManager.createDirectoryAtPath(dir, withIntermediateDirectories = true, attributes = null, error = null)
    return dir
}

private fun imagePath(playlistId: String, imageId: String): String {
    return "${imageDir(playlistId)}/$imageId.jpg"
}

actual suspend fun saveImage(playlistId: String, imageId: String, file: FileReference) {
    val sourceUrl = NSURL.fileURLWithPath(file.filePath)
    val destPath = imagePath(playlistId, imageId)
    val destUrl = NSURL.fileURLWithPath(destPath)
    NSFileManager.defaultManager.copyItemAtURL(sourceUrl, toURL = destUrl, error = null)
}

actual suspend fun loadImageSource(playlistId: String, imageId: String): ImageSource? {
    val path = imagePath(playlistId, imageId)
    if (!NSFileManager.defaultManager.fileExistsAtPath(path)) return null
    return ImageLocal(FileReference(path))
}

actual fun deleteImage(playlistId: String, imageId: String) {
    val path = imagePath(playlistId, imageId)
    NSFileManager.defaultManager.removeItemAtPath(path, error = null)
}

actual suspend fun saveImageFromBlob(playlistId: String, imageId: String, blob: com.lightningkite.kiteui.Blob) {
    val bytes = blob.toByteArray()
    val destPath = imagePath(playlistId, imageId)
    val data = bytes.toNSData()
    data.writeToFile(destPath, atomically = true)
}

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData {
    return kotlinx.cinterop.memScoped {
        NSData.dataWithBytes(kotlinx.cinterop.allocArrayOf(this@toNSData), this@toNSData.size.toULong())
    }
}

actual fun deletePlaylistImages(playlistId: String) {
    val dir = imageDir(playlistId)
    NSFileManager.defaultManager.removeItemAtPath(dir, error = null)
}
