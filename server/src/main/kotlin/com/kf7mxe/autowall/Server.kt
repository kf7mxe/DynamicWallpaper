package com.kf7mxe.autowall

import com.lightningkite.lightningserver.auth.*
import com.lightningkite.lightningserver.cors.CorsInterceptor
import com.lightningkite.lightningserver.cors.CorsSettings
import com.lightningkite.lightningserver.definition.Runtime
import com.lightningkite.lightningserver.definition.builder.ServerBuilder
import com.lightningkite.lightningserver.files.FileSystemEndpoints
import com.lightningkite.lightningserver.files.UploadEarlyEndpoint
import com.lightningkite.lightningserver.http.*
import com.lightningkite.lightningserver.plainText
import com.lightningkite.lightningserver.serialization.registerBasicMediaTypeCoders
import com.lightningkite.lightningserver.typed.MetaEndpoints
import com.lightningkite.lightningserver.typed.sdk.module
import com.lightningkite.lightningserver.websockets.MultiplexWebSocketHandler
import com.lightningkite.lightningserver.websockets.QueryParamWebSocketHandler
import com.kf7mxe.autowall.UserAuth.RoleCache.userRole
import com.kf7mxe.autowall.data.*
import com.kf7mxe.autowall.iap.GoogleIAPSettings
import com.lightningkite.services.cache.Cache
import com.lightningkite.services.database.Database
import com.lightningkite.services.database.jsonfile.JsonFileDatabase
import com.lightningkite.services.database.mongodb.MongoDatabase
import com.lightningkite.services.email.EmailService
import com.lightningkite.services.email.javasmtp.JavaSmtpEmailService
import com.lightningkite.services.files.PublicFileSystem
import com.lightningkite.services.files.s3.S3PublicFileSystem

object Server : ServerBuilder() {

    // Settings
    val cache = setting("cache", Cache.Settings())
    val database = setting("database", Database.Settings())
    val email = setting("email", EmailService.Settings())
    val webUrl = setting("webUrl", "http://localhost:8080")
    val cors = setting("cors", CorsSettings())
    val files = setting("files", PublicFileSystem.Settings())

    val googleIAP = setting("googleIAP", GoogleIAPSettings("mock"))

    init {
        install(CorsInterceptor(cors))
        registerBasicMediaTypeCoders()

        MongoDatabase
        JsonFileDatabase
        JavaSmtpEmailService
        S3PublicFileSystem

        AuthRequirement.isSuperUser = UserAuth.require { it.userRole() >= UserRole.ROOT }
    }

    // Endpoints

    val products = path.path("products") module ProductEndpoints
    val subscriptions = path.path("subscriptions") module SubscriptionEndpoints



    val root = path.get bind HttpHandler {
        HttpResponse.plainText("Welcome to AutoWall API!")
    }

    val uploadEarly = path.path("upload-early") module UploadEarlyEndpoint(
        files = files,
        database = database,
        fileScanner = Runtime.Constant(emptyList())
    )
    val localFileServer = path.path("files") include FileSystemEndpoints(files)

    val users = path.path("users") module UserEndpoints
    val authEndpoints = path.path("auth") module UserAuth
    val playlists = path.path("playlists") module PlaylistEndpoints
    val storeWallpaperPacks = path.path("store-wallpaper-packs") module StoreWallpaperPackEndpoints
    val wallpaperPacks = path.path("wallpaper-packs") module WallpaperPackEndpoints
    val wallpapers = path.path("wallpapers") module WallpaperEndpoints

    val multiplex = path.path("multiplex") bind MultiplexWebSocketHandler()
    val base = path bind QueryParamWebSocketHandler()
    val meta = path.path("meta") module MetaEndpoints(
        "com.kf7mxe.autowall",
        database,
        cache
    )
}
