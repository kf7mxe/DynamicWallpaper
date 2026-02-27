package com.dynamicwallpaper.storage

import com.lightningkite.kiteui.Blob
import com.lightningkite.kiteui.FileReference
import com.lightningkite.kiteui.models.ImageSource

/**
 * Save an image from a picked FileReference to persistent local storage.
 * @param playlistId The playlist this image belongs to
 * @param imageId A unique ID for this image (UUID string)
 * @param file The FileReference from the image picker
 */
expect suspend fun saveImage(playlistId: String, imageId: String, file: FileReference)

/**
 * Load a previously saved image as an ImageSource for display.
 * Returns null if the image doesn't exist.
 */
expect suspend fun loadImageSource(playlistId: String, imageId: String): ImageSource?

/**
 * Delete a single image from storage.
 */
expect fun deleteImage(playlistId: String, imageId: String)

/**
 * Save an image from a downloaded Blob to persistent local storage.
 * Used when downloading images from the server.
 */
expect suspend fun saveImageFromBlob(playlistId: String, imageId: String, blob: Blob)

/**
 * Delete all images for a playlist.
 */
expect fun deletePlaylistImages(playlistId: String)
