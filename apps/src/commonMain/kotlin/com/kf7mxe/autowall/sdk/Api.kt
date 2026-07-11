package com.kf7mxe.autowall.sdk



interface Api {
	fun withHeaderCalculator(calculator: suspend () -> List<Pair<String, String>>): Api

	interface GoogleIAPApi {
		/**
		 * Google IAP Environment
		 * 
		 * Returns the servers Google Environment
		 * 
		 * **Auth Requirements:** User with root access
		 * */
		suspend fun googleIAPEnvironment(): com.kf7mxe.autowall.GoogleIAPEnvironment
		/**
		 * Validate Google Receipt
		 * 
		 * Validates a users Google IAP store receipt
		 * 
		 * **Auth Requirements:** User with root access
		 * */
		suspend fun validateGoogleReceipt(input: List<com.kf7mxe.autowall.GoogleReceiptValidationRequest>)
		/**
		 * Restore Google Purchases
		 * 
		 * Validates a users Google IAP store receipt and will move subscription to authenticated User
		 * 
		 * **Auth Requirements:** User with root access
		 * */
		suspend fun restoreGooglePurchases(input: List<com.kf7mxe.autowall.GoogleReceiptValidationRequest>)
	}
	val googleIAP: GoogleIAPApi

	val product: com.lightningkite.lightningserver.typed.ClientModelRestEndpoints<com.kf7mxe.autowall.Product, kotlin.String>

	val subscription: com.lightningkite.lightningserver.typed.ClientModelRestEndpoints<com.kf7mxe.autowall.Subscription, kotlin.uuid.Uuid>

	val uploadEarlyEndpoint: com.lightningkite.lightningserver.files.ClientUploadEarlyEndpoints

	val user: com.lightningkite.lightningserver.typed.ClientModelRestEndpoints<com.kf7mxe.autowall.User, kotlin.uuid.Uuid>

	interface UserAuthApi : com.lightningkite.lightningserver.sessions.proofs.AuthClientEndpoints<com.kf7mxe.autowall.User, kotlin.uuid.Uuid>, com.lightningkite.lightningserver.typed.ClientModelRestEndpoints<com.lightningkite.lightningserver.sessions.Session<com.kf7mxe.autowall.User, kotlin.uuid.Uuid>, kotlin.uuid.Uuid> {

		val email: com.lightningkite.lightningserver.sessions.proofs.ProofClientEndpoints.Email

		interface PasswordProof : com.lightningkite.lightningserver.typed.ClientModelRestEndpoints<com.lightningkite.lightningserver.sessions.PasswordSecret, kotlin.uuid.Uuid>, com.lightningkite.lightningserver.sessions.proofs.ProofClientEndpoints.Password {
		}
		val password: PasswordProof
	}
	val userAuth: UserAuthApi

	val playlist: com.lightningkite.lightningserver.typed.ClientModelRestEndpoints<com.kf7mxe.autowall.Playlist, kotlin.uuid.Uuid>

	val subPlaylist: com.lightningkite.lightningserver.typed.ClientModelRestEndpoints<com.kf7mxe.autowall.SubPlaylist, kotlin.uuid.Uuid>

	val storeWallpaperPack: com.lightningkite.lightningserver.typed.ClientModelRestEndpoints<com.kf7mxe.autowall.StoreWallpaperPack, kotlin.uuid.Uuid>

	val wallpaperPack: com.lightningkite.lightningserver.typed.ClientModelRestEndpoints<com.kf7mxe.autowall.WallpaperPack, kotlin.uuid.Uuid>

	val wallpaper: com.lightningkite.lightningserver.typed.ClientModelRestEndpoints<com.kf7mxe.autowall.Wallpaper, kotlin.uuid.Uuid>

	interface MetaApi {
		/**
		 * Get Server Health
		 * 
		 * Gets the current status of the server
		 * 
		 * **Auth Requirements:** No Requirements
		 * */
		suspend fun getServerHealth(): com.lightningkite.lightningserver.typed.ServerHealth
		/**
		 * Bulk Request
		 * 
		 * Performs multiple requests at once, returning the results in the same order.
		 * 
		 * **Auth Requirements:** No Requirements
		 * */
		suspend fun bulkRequest(input: Map<String, com.lightningkite.lightningserver.typed.BulkRequest>): Map<String, com.lightningkite.lightningserver.typed.BulkResponse>
	}
	val meta: MetaApi
}
