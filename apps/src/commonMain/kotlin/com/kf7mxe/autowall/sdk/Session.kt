package com.foodecision.sdk

import com.kf7mxe.autowall.WallpaperPack
import com.kf7mxe.autowall.Playlist
import com.kf7mxe.autowall.Subscription
import com.kf7mxe.autowall.sdk.Api
import com.kf7mxe.autowall.sdk.CachedApi
import com.kf7mxe.autowall.sdk.LiveApi
import com.kf7mxe.autowall.sdk.ModelOfflineSyncStoreApi
import com.lightningkite.kiteui.Platform
import com.lightningkite.kiteui.current
import com.lightningkite.kiteui.reactive.*
import com.lightningkite.kiteui.suppressConnectivityIssues
import com.lightningkite.lightningserver.LsErrorException
import com.lightningkite.lightningserver.auth.accessToken
import com.lightningkite.lightningserver.files.expires
import com.lightningkite.lightningserver.sessions.expires
import com.lightningkite.lightningserver.typed.user
import com.lightningkite.reactive.context.*
import com.lightningkite.reactive.core.*
import com.lightningkite.services.database.Condition
import com.lightningkite.services.database.Query
import com.lightningkite.services.database.and
import com.lightningkite.services.database.condition
import com.lightningkite.services.database.eq
import com.lightningkite.services.database.gt
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.let
import kotlin.time.Clock.System.now
import kotlin.uuid.Uuid

val subscriptionRefresh = BasicListenable()

val API_TIMEOUT = 5.seconds


class UserSession(val api: Api?, val userId: Uuid?) {
    val serverCached = api?.let { CachedApi(api) }

    val playlists: ModelOfflineSyncStoreApi<Playlist, Uuid, Uuid> =
        ModelOfflineSyncStoreApi(
            serverCached?.playlists,
            Playlist.serializer(),
            Playlist::class.simpleName ?: "Playlist",
            condition { it.user.eq(userId) },
            { recipesMediaToUpload ->
                emptyList()
            },
            { remoteItems ->
                emptyList()
            },
            { model, userId: Uuid ->
                model.copy(user = userId) }
        )

    val packs: ModelOfflineSyncStoreApi<WallpaperPack, Uuid, Uuid> =
        ModelOfflineSyncStoreApi(
            serverCached?.packs,
            WallpaperPack.serializer(),
            WallpaperPack::class.simpleName ?: "MealPlan",
            condition<WallpaperPack> { it.user.eq(userId) },
            null,
            null,
            { model, userId -> model.copy(user = userId) }
        )


    val me = rememberSuspending { userId?.let { userId -> serverCached?.users[userId]?.invoke() } }

    private val _mySubscriptions = remember {
        rerunOn(subscriptionRefresh)
        val now = now()
        userId?.let { userId ->
            serverCached?.subscription?.query(
                Query(
                    condition {
                        it.user.eq(userId) and it.expires.gt(now) and it.startTime.lte(now)
                    }
                )
            )
        }
    }

    val mySubscriptions: Reactive<List<Subscription>> = remember {
        _mySubscriptions()?.let { it() } ?: emptyList()
    }

    private val allServerProducts = rememberSuspending {
        api2?.product?.query(Query(Condition.Always))
        //         session().serverCached?.product?.query(Query(Condition.Always))
    }

    val serverProducts = remember {
        allServerProducts()?.filter {
            it.storeType == Platform.current.toStoreType() && !it.archived
        }
    }
}

val hasSubscription = remember {
    (session().mySubscriptions().isNotEmpty() || (session().me()?.role ?: Role.User) >= Role.Tester)
}

private suspend fun registerToken(authApi: Api2) {
    suppressConnectivityIssues {
        //        fcmToken()?.takeIf { it.isNotEmpty() }?.let { authApi.fcmToken.registerToken(it) }
    }
}

var subscriptionChecked = false

val rawSession = rememberSuspending {
    val token = sessionToken()
    println("DEVUG 1")
    val authApi =
        token?.let { token ->
            val api = selectedApi().api
            api.withHeaderCalculator(api.userAuth.accessToken(token))
        }
    println("DEVUG 2")

    val self =
        try {
            withTimeout(20.seconds) {
                return@withTimeout authApi?.userAuth?.getSelf()
            }
        } catch (e: Exception) {
            if (e is TimeoutCancellationException) null
            if(  e is LsErrorException && e.status == 401.toShort()) {
                println("DEBUG E ${e.status}")
                sessionToken.set(null)
                null
            }
            null
        }
    println("DEBUG 3")

    if (!subscriptionChecked) {
        subscriptionChecked = true
        AppScope.launch { CommonSubscriptionManager.checkSubscriptions() }
    }

    UserSession(
        api2 = authApi,
        userId = self?._id,
    )
        .also {
            //            AppScope.launch { registerToken(authApi) }
        }
    //        }
    //    } catch (e: TimeoutCancellationException) {
    //
    //    }

}

val currentSessionFailed = BasicListenable()

val session = remember {
    val result = rawSession.invoke()
    if (result == null) {
        currentSessionFailed.invokeAll()
        //        launch { deregisterToken() }
        throw CancellationException("No session found")
    }
    result
}

class UnAuthSession(val api: LiveApi) {
    val serverCached = CachedApi(api)
}

val unAuthSession = remember { UnAuthSession(selectedApi().api) }

val sessionToken = PersistentProperty<String?>("sessionToken", null)
