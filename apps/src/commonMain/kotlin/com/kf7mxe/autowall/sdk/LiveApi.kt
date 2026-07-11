package com.kf7mxe.autowall.sdk

import com.lightningkite.lightningserver.HttpMethod
import com.lightningkite.lightningserver.typed.Fetcher
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable

class LiveApi(val fetcher: Fetcher) : Api {
	override fun withHeaderCalculator(calculator: suspend () -> List<Pair<String, String>>): LiveApi = 
		LiveApi(fetcher.withHeaderCalculator(calculator))

	inner class LiveGoogleIAPApi : Api.GoogleIAPApi {
		override suspend fun googleIAPEnvironment(): com.kf7mxe.autowall.GoogleIAPEnvironment =
			fetcher("iap/google/environment", HttpMethod.POST, kotlin.Unit.serializer(), kotlin.Unit, com.kf7mxe.autowall.GoogleIAPEnvironment.serializer())
		override suspend fun validateGoogleReceipt(input: List<com.kf7mxe.autowall.GoogleReceiptValidationRequest>) =
			fetcher("iap/google/validate-receipt", HttpMethod.POST, ListSerializer(com.kf7mxe.autowall.GoogleReceiptValidationRequest.serializer()), input, kotlin.Unit.serializer())
		override suspend fun restoreGooglePurchases(input: List<com.kf7mxe.autowall.GoogleReceiptValidationRequest>) =
			fetcher("iap/google/restore-purchase", HttpMethod.POST, ListSerializer(com.kf7mxe.autowall.GoogleReceiptValidationRequest.serializer()), input, kotlin.Unit.serializer())
	}
	override val googleIAP = LiveGoogleIAPApi()

	override val product = com.lightningkite.lightningserver.typed.LiveClientModelRestEndpoints(fetcher, "products", com.kf7mxe.autowall.Product.serializer(), kotlin.String.serializer())

	override val subscription = com.lightningkite.lightningserver.typed.LiveClientModelRestEndpoints(fetcher, "subscriptions", com.kf7mxe.autowall.Subscription.serializer(), kotlin.uuid.Uuid.serializer())

	override val uploadEarlyEndpoint = com.lightningkite.lightningserver.files.LiveClientUploadEarlyEndpoints(fetcher, "upload-early", )

	override val user = com.lightningkite.lightningserver.typed.LiveClientModelRestEndpoints(fetcher, "users", com.kf7mxe.autowall.User.serializer(), kotlin.uuid.Uuid.serializer())

	inner class LiveUserAuthApi : Api.UserAuthApi, com.lightningkite.lightningserver.sessions.proofs.AuthClientEndpoints<com.kf7mxe.autowall.User, kotlin.uuid.Uuid> by com.lightningkite.lightningserver.sessions.proofs.LiveAuthClientEndpoints(fetcher, "auth/session", com.kf7mxe.autowall.User.serializer(), kotlin.uuid.Uuid.serializer()), com.lightningkite.lightningserver.typed.ClientModelRestEndpoints<com.lightningkite.lightningserver.sessions.Session<com.kf7mxe.autowall.User, kotlin.uuid.Uuid>, kotlin.uuid.Uuid> by com.lightningkite.lightningserver.typed.LiveClientModelRestEndpoints(fetcher, "auth/session/sessions", com.lightningkite.lightningserver.sessions.Session.serializer(com.kf7mxe.autowall.User.serializer(), kotlin.uuid.Uuid.serializer()), kotlin.uuid.Uuid.serializer()) {

		override val email = com.lightningkite.lightningserver.sessions.proofs.LiveProofClientEndpoints.Email(fetcher, "auth/proof/email", )

		inner class LivePasswordProof : Api.UserAuthApi.PasswordProof, com.lightningkite.lightningserver.typed.ClientModelRestEndpoints<com.lightningkite.lightningserver.sessions.PasswordSecret, kotlin.uuid.Uuid> by com.lightningkite.lightningserver.typed.LiveClientModelRestEndpoints(fetcher, "auth/proof/password/secrets", com.lightningkite.lightningserver.sessions.PasswordSecret.serializer(), kotlin.uuid.Uuid.serializer()), com.lightningkite.lightningserver.sessions.proofs.ProofClientEndpoints.Password by com.lightningkite.lightningserver.sessions.proofs.LiveProofClientEndpoints.Password(fetcher, "auth/proof/password", ) {
		}
		override val password = LivePasswordProof()
	}
	override val userAuth = LiveUserAuthApi()

	override val playlist = com.lightningkite.lightningserver.typed.LiveClientModelRestEndpoints(fetcher, "playlists", com.kf7mxe.autowall.Playlist.serializer(), kotlin.uuid.Uuid.serializer())

	override val subPlaylist = com.lightningkite.lightningserver.typed.LiveClientModelRestEndpoints(fetcher, "sub-playlist", com.kf7mxe.autowall.SubPlaylist.serializer(), kotlin.uuid.Uuid.serializer())

	override val storeWallpaperPack = com.lightningkite.lightningserver.typed.LiveClientModelRestEndpoints(fetcher, "store-wallpaper-packs", com.kf7mxe.autowall.StoreWallpaperPack.serializer(), kotlin.uuid.Uuid.serializer())

	override val wallpaperPack = com.lightningkite.lightningserver.typed.LiveClientModelRestEndpoints(fetcher, "wallpaper-packs", com.kf7mxe.autowall.WallpaperPack.serializer(), kotlin.uuid.Uuid.serializer())

	override val wallpaper = com.lightningkite.lightningserver.typed.LiveClientModelRestEndpoints(fetcher, "wallpapers", com.kf7mxe.autowall.Wallpaper.serializer(), kotlin.uuid.Uuid.serializer())

	inner class LiveMetaApi : Api.MetaApi {
		override suspend fun getServerHealth(): com.lightningkite.lightningserver.typed.ServerHealth =
			fetcher("meta/health", HttpMethod.GET, kotlin.Unit.serializer(), kotlin.Unit, com.lightningkite.lightningserver.typed.ServerHealth.serializer())
		override suspend fun bulkRequest(input: Map<String, com.lightningkite.lightningserver.typed.BulkRequest>): Map<String, com.lightningkite.lightningserver.typed.BulkResponse> =
			fetcher("meta/bulk", HttpMethod.POST, MapSerializer(String.serializer(), com.lightningkite.lightningserver.typed.BulkRequest.serializer()), input, MapSerializer(String.serializer(), com.lightningkite.lightningserver.typed.BulkResponse.serializer()))
	}
	override val meta = LiveMetaApi()
}
