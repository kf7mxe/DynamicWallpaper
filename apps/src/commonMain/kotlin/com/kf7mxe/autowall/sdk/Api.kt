package com.kf7mxe.autowall.sdk



interface Api {
	fun withHeaderCalculator(calculator: suspend () -> List<Pair<String, String>>): Api

	interface UploadEarlyEndpointApi {
		/**
		 * Upload File for Request
		 * 
		 * Upload a file to make a request later.  Times out in around 10 minutes.
		 * 
		 * **Auth Requirements:** No Requirements
		 * */
		suspend fun uploadFileForRequest(): com.lightningkite.lightningserver.files.UploadInformation
		/**
		 * Verify uploaded file
		 * 
		 * Checks out a file and moves it out of jail if it's safe.  Makes for significantly faster subsequent requests.
		 * 
		 * **Auth Requirements:** No Requirements
		 * */
		suspend fun verifyUploadedFile(input: kotlin.String): kotlin.String
	}
	val uploadEarlyEndpoint: UploadEarlyEndpointApi

	val user: com.lightningkite.lightningserver.typed.ClientModelRestEndpoints<com.kf7mxe.autowall.User, kotlin.uuid.Uuid>

	interface UserAuthApi : com.lightningkite.lightningserver.sessions.proofs.AuthClientEndpoints<com.kf7mxe.autowall.User, kotlin.uuid.Uuid>, com.lightningkite.lightningserver.typed.ClientModelRestEndpoints<com.lightningkite.lightningserver.sessions.Session<com.kf7mxe.autowall.User, kotlin.uuid.Uuid>, kotlin.uuid.Uuid> {

		val email: com.lightningkite.lightningserver.sessions.proofs.ProofClientEndpoints.Email

		interface PasswordProof : com.lightningkite.lightningserver.typed.ClientModelRestEndpoints<com.lightningkite.lightningserver.sessions.PasswordSecret, kotlin.uuid.Uuid>, com.lightningkite.lightningserver.sessions.proofs.ProofClientEndpoints.Password {
		}
		val password: PasswordProof
	}
	val userAuth: UserAuthApi

	val playlist: com.lightningkite.lightningserver.typed.ClientModelRestEndpoints<com.kf7mxe.autowall.Playlist, kotlin.uuid.Uuid>

	val wallpaperPack: com.lightningkite.lightningserver.typed.ClientModelRestEndpoints<com.kf7mxe.autowall.WallpaperPack, kotlin.uuid.Uuid>

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
