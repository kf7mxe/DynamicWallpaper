package com.kf7mxe.autowall.sdk

import com.lightningkite.lightningserver.db.*
import kotlinx.serialization.builtins.*

open class CachedApi(val uncached: Api) {
	open val users = ModelCache(uncached.user, com.kf7mxe.autowall.User.serializer())
	open val sessions = ModelCache(uncached.userAuth, com.lightningkite.lightningserver.sessions.Session.serializer(com.kf7mxe.autowall.User.serializer(), kotlin.uuid.Uuid.serializer()))
	open val passwordSecrets = ModelCache(uncached.userAuth.password, com.lightningkite.lightningserver.sessions.PasswordSecret.serializer())
	open val playlists = ModelCache(uncached.playlist, com.kf7mxe.autowall.Playlist.serializer())
	open val packs = ModelCache(uncached.wallpaperPack, com.kf7mxe.autowall.WallpaperPack.serializer())
}
