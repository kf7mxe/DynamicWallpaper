package com.foodecision.sdk

import com.beemee.iap.CommonSubscriptionManager
import com.kf7mxe.autowall.WallpaperPack
import com.kf7mxe.autowall.Playlist
import com.kf7mxe.autowall.SubPlaylist
import com.kf7mxe.autowall.Subscription
import com.kf7mxe.autowall.UserRole
import com.kf7mxe.autowall.expires
import com.kf7mxe.autowall.sdk.Api
import com.kf7mxe.autowall.sdk.CachedApi
import com.kf7mxe.autowall.sdk.LiveApi
import com.kf7mxe.autowall.sdk.ModelOfflineSyncStoreApi
import com.kf7mxe.autowall.sdk.selectedApi
import com.kf7mxe.autowall.startTime
import com.kf7mxe.autowall.toStoreType
import com.kf7mxe.autowall.user
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
import com.lightningkite.services.database.lte
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

val invalidateToken = BasicListenable()


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

    val subPlaylist: ModelOfflineSyncStoreApi<SubPlaylist, Uuid, Uuid> =
        ModelOfflineSyncStoreApi(
            serverCached?.subPlaylists,
            SubPlaylist.serializer(),
            SubPlaylist::class.simpleName ?: "SubPlaylist",
            condition { it.user.eq(userId) },
            {media ->
                emptyList()
            },
            {
                emptyList()
            },
            { model, userId: Uuid ->
                model.copy(user = userId)
            }
        )

    val wallpaperPacks: ModelOfflineSyncStoreApi<WallpaperPack, Uuid, Uuid> =
        ModelOfflineSyncStoreApi(
            serverCached?.wallpaperPacks,
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
            serverCached?.subscriptions?.query(
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
        api?.product?.query(Query(Condition.Always))
        //         session().serverCached?.product?.query(Query(Condition.Always))
    }

    val serverProducts = remember {
        allServerProducts()?.filter {
            it.storeType == Platform.current.toStoreType() && !it.archived
        }
    }
}

val hasSubscription = remember {
    (loggedInOrNull()?.mySubscriptions()?.isNotEmpty() == true || (loggedInOrNull()?.me()?.role ?: UserRole.USER) >= UserRole.ADMIN)
}

private suspend fun registerToken(authApi: Api) {
    suppressConnectivityIssues {
        //        fcmToken()?.takeIf { it.isNotEmpty() }?.let { authApi.fcmToken.registerToken(it) }
    }
}

var subscriptionChecked = false

val loggedInOrNull = rememberSuspending {
    val token = sessionToken() ?: return@rememberSuspending null
    println("DEVUG 1")
    val api = selectedApi().api

    val authApi = api.withHeaderCalculator(api.userAuth.accessToken(token, invalidateToken))


//    val self =
//        try {
//            withTimeout(20.seconds) {
//                return@withTimeout authApi?.userAuth?.getSelf()
//            }
//        } catch (e: Exception) {
//            if (e is TimeoutCancellationException) null
//            if(  e is LsErrorException && e.status == 401.toShort()) {
//                println("DEBUG E ${e.status}")
//                sessionToken.set(null)
//                null
//            }
//            null
//        }
//    println("DEBUG 3")

    if (!subscriptionChecked) {
        subscriptionChecked = true
        AppScope.launch {  CommonSubscriptionManager.checkSubscriptions() }
    }

    try {
        val self = authApi.userAuth.getSelf()

        UserSession(
            api = authApi,
            userId = self._id,
        )
    } catch (e: Exception) {
        println("FAILED")
        e.printStackTrace()
        null
    }

}

val currentSessionFailed = BasicListenable()

val session= remember {
    loggedInOrNull() ?: UserSession(api = selectedApi().api, userId = null)

}

class UnAuthSession(val api: LiveApi) {
    val serverCached = CachedApi(api)
}

val unAuthSession = remember { UnAuthSession(selectedApi().api) }

val sessionToken = PersistentProperty<String?>("sessionToken", null)
