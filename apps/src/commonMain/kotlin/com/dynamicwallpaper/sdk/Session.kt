package com.dynamicwallpaper.sdk

import com.lightningkite.lightningserver.auth.accessToken
import com.lightningkite.lightningserver.networking.ConnectivityFetcher
import com.lightningkite.reactive.core.BasicListenable
import com.lightningkite.reactive.core.Signal
import com.lightningkite.reactive.context.invoke
import com.lightningkite.reactive.core.rememberSuspending
import com.dynamicwallpaper.storage.readLocalFile
import com.dynamicwallpaper.storage.writeLocalFile
import kotlin.coroutines.cancellation.CancellationException
import kotlin.uuid.Uuid

private const val SESSION_TOKEN_FILE = "session_token"
private const val SELECTED_API_FILE = "selected_api"

val selectedApi: Signal<ApiOption> = Signal(loadSelectedApi())

val sessionToken: Signal<String?> = Signal(readLocalFile(SESSION_TOKEN_FILE)?.takeIf { it.isNotBlank() })

fun saveSessionToken(token: String?) {
    sessionToken.value = token
    writeLocalFile(SESSION_TOKEN_FILE, token ?: "")
}

fun saveSelectedApi(option: ApiOption) {
    selectedApi.value = option
    writeLocalFile(SELECTED_API_FILE, option.name)
}

private fun loadSelectedApi(): ApiOption {
    val saved = readLocalFile(SELECTED_API_FILE)?.trim() ?: return getDefaultServerBackend()
    return try {
        ApiOption.valueOf(saved)
    } catch (e: Exception) {
        getDefaultServerBackend()
    }
}

data class UserSession(val api: Api, val userId: Uuid) : CachedApi(api)

val invalidateToken = BasicListenable()

fun createUnauthApi(): Api {
    val apiOption = selectedApi.value
    val fetcher = ConnectivityFetcher(apiOption.http, apiOption.ws)
    return LiveApi(fetcher)
}

val rawSession = rememberSuspending<UserSession?> {
    val token = sessionToken() ?: return@rememberSuspending null
    val api = createUnauthApi()

    val authApi = api.withHeaderCalculator(api.userAuth.accessToken(token, invalidateToken))
    try {
        val self = authApi.userAuth.getSelf()
        UserSession(
            api = authApi,
            userId = self._id,
        )
    } catch (e: Exception) {
        println("Session restore failed")
        e.printStackTrace()
        null
    }
}

val currentSessionFailed = BasicListenable()

val session = rememberSuspending {
    val result = rawSession()
    if (result == null) {
        currentSessionFailed.invokeAll()
        throw CancellationException("No session found")
    }
    result
}

fun logout() {
    saveSessionToken(null)
    invalidateToken.invokeAll()
}
