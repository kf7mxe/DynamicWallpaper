package com.kf7mxe.autowall.engine

import kotlin.uuid.Uuid

expect suspend fun setWallpaper(playlistId: String, imageId: Uuid)
