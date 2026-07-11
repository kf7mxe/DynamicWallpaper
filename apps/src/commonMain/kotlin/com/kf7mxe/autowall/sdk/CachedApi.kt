package com.kf7mxe.autowall.sdk

import com.lightningkite.lightningserver.db.*
import kotlinx.serialization.builtins.*

open class CachedApi(val uncached: Api) {
	open val products = ModelCache(uncached.product, com.kf7mxe.autowall.Product.serializer())
	open val subscriptions = ModelCache(uncached.subscription, com.kf7mxe.autowall.Subscription.serializer())
	open val users = ModelCache(uncached.user, com.kf7mxe.autowall.User.serializer())
	open val sessions = ModelCache(uncached.userAuth, com.lightningkite.lightningserver.sessions.Session.serializer(com.kf7mxe.autowall.User.serializer(), kotlin.uuid.Uuid.serializer()))
	open val passwordSecrets = ModelCache(uncached.userAuth.password, com.lightningkite.lightningserver.sessions.PasswordSecret.serializer())
	open val playlists = ModelCache(uncached.playlist, com.kf7mxe.autowall.Playlist.serializer())
	open val subPlaylists = ModelCache(uncached.subPlaylist, com.kf7mxe.autowall.SubPlaylist.serializer())
	open val storeWallpaperPacks = ModelCache(uncached.storeWallpaperPack, com.kf7mxe.autowall.StoreWallpaperPack.serializer())
	open val wallpaperPacks = ModelCache(uncached.wallpaperPack, com.kf7mxe.autowall.WallpaperPack.serializer())
	open val wallpapers = ModelCache(uncached.wallpaper, com.kf7mxe.autowall.Wallpaper.serializer())
}
