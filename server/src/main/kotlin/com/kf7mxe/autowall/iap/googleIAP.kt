package com.kf7mxe.autowall.iap

import com.google.auth.oauth2.GoogleCredentials
import com.kf7mxe.autowall.*
import com.kf7mxe.autowall.UserAuth
import com.lightningkite.lightningserver.BadRequestException
import com.lightningkite.lightningserver.auth.fetch
import com.lightningkite.lightningserver.auth.require
import com.lightningkite.lightningserver.definition.builder.ServerBuilder
import com.lightningkite.lightningserver.http.post
import com.lightningkite.lightningserver.pathing.PathSpec0
import com.lightningkite.lightningserver.runtime.ServerRuntime
import com.lightningkite.lightningserver.typed.ApiHttpHandler
import com.lightningkite.lightningserver.typed.auth
import com.lightningkite.lightningserver.typed.explicitApiHttpHandler
import com.lightningkite.services.Setting
import com.lightningkite.services.SettingContext
import com.lightningkite.services.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.serializer
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlin.uuid.Uuid
import java.io.File
import java.net.HttpURLConnection
import java.net.URI

@Serializable
data class GoogleIAPSettings(
    val url: String,
) : Setting<GoogleApi> {

    override fun invoke(name: String, context: SettingContext): GoogleApi {
        val protocol = url.substringBefore("://")
        val data = url.substringAfter("://")
        return when (protocol) {
            "mock" -> MockGoogleAPI()
            "google" -> {
                val creds = if (data.startsWith("{")) data else File(data).readText()
                LiveGoogleApi(creds)
            }
            else -> throw IllegalArgumentException("Unknown GoogleIAP type: $protocol")
        }
    }
}

object GoogleIAP : ServerBuilder() {
    private val basePath = path.path("google")

    private val envHandler: ApiHttpHandler<PathSpec0, User, Unit, GoogleIAPEnvironment> =
        explicitApiHttpHandler(
            summary = "Google IAP Environment",
            description = "Returns the servers Google Environment",
            inputType = serializer<Unit>(),
            outputType = GoogleIAPEnvironment.serializer(),
            auth = UserAuth.require(),
            implementation = { _: Unit ->
                Server.googleIAP().environment
            }
        )

    val getEnvironment = basePath.path("environment").post bind envHandler

    private val validateHandler: ApiHttpHandler<PathSpec0, User, List<GoogleReceiptValidationRequest>, Unit> =
        explicitApiHttpHandler(
            summary = "Validate Google Receipt",
            description = "Validates a users Google IAP store receipt",
            inputType = ListSerializer(GoogleReceiptValidationRequest.serializer()),
            outputType = serializer<Unit>(),
            auth = UserAuth.require(),
            implementation = { receipts: List<GoogleReceiptValidationRequest> ->
                val api = Server.googleIAP()
                val userId = this.auth.fetch()._id
                api.validateReceiptImplementation(userId, receipts)
            }
        )

    val validateReceipt = basePath.path("validate-receipt").post bind validateHandler

    private val restoreHandler: ApiHttpHandler<PathSpec0, User, List<GoogleReceiptValidationRequest>, Unit> =
        explicitApiHttpHandler(
            summary = "Restore Google Purchases",
            description = "Validates a users Google IAP store receipt and will move subscription to authenticated User",
            inputType = ListSerializer(GoogleReceiptValidationRequest.serializer()),
            outputType = serializer<Unit>(),
            auth = UserAuth.require(),
            implementation = { receipts: List<GoogleReceiptValidationRequest> ->
                val api = Server.googleIAP()
                val userId = this.auth.fetch()._id
                api.restorePurchaseImplementation(userId, receipts)
            }
        )

    val restorePurchase = basePath.path("restore-purchase").post bind restoreHandler
}

interface GoogleApi {
    val environment: GoogleIAPEnvironment
    context(server: ServerRuntime)
    suspend fun validateReceiptImplementation(
        userId: Uuid,
        receipts: List<GoogleReceiptValidationRequest>,
    )
    context(server: ServerRuntime)
    suspend fun restorePurchaseImplementation(
        userId: Uuid,
        receipts: List<GoogleReceiptValidationRequest>,
    )
}

class MockGoogleAPI : GoogleApi {

    override val environment: GoogleIAPEnvironment = GoogleIAPEnvironment.Mock

    context(server: ServerRuntime)
    override suspend fun validateReceiptImplementation(
        userId: Uuid,
        receipts: List<GoogleReceiptValidationRequest>,
    ) {
        val receipt = receipts.firstOrNull() ?: return

        Server.subscriptions.info.table().upsertOne(
            condition { it.user.eq(userId) },
            modification {
                it.product assign receipt.purchaseToken
                it.expires assign Clock.System.now().plus(31.days)
            },
            Subscription(
                user = userId,
                transactionId = null,
                storeType = StoreType.Google,
                product = receipt.purchaseToken,
                expires = Clock.System.now().plus(31.days),
                startTime = Clock.System.now(),
                autoRenewing = true,
            )
        )
    }

    context(server: ServerRuntime)
    override suspend fun restorePurchaseImplementation(
        userId: Uuid,
        receipts: List<GoogleReceiptValidationRequest>,
    ) {
        val receipt = receipts.firstOrNull() ?: return

        Server.subscriptions.info.table().upsertOne(
            condition { it.user.eq(userId) },
            modification {
                it.product assign receipt.purchaseToken
                it.expires assign Clock.System.now().plus(31.days)
            },
            Subscription(
                user = userId,
                transactionId = null,
                storeType = StoreType.Google,
                product = receipt.purchaseToken,
                expires = Clock.System.now().plus(31.days),
                startTime = Clock.System.now(),
                autoRenewing = true,
            )
        )
    }
}

class LiveGoogleApi(jsonCreds: String) : GoogleApi {

    private val credentials: GoogleCredentials = GoogleCredentials
        .fromStream(jsonCreds.byteInputStream())
        .createScoped("https://www.googleapis.com/auth/androidpublisher")

    private val json = Json { ignoreUnknownKeys = true }

    private suspend fun getSubPurchase(receipt: GoogleReceiptValidationRequest): SubscriptionPurchaseV2 =
        withContext(Dispatchers.IO) {
            try {
                credentials.refreshIfExpired()
                val url = URI(
                    "https://androidpublisher.googleapis.com/androidpublisher/v3/applications/${receipt.packageName}/purchases/subscriptionsv2/tokens/${receipt.purchaseToken}"
                ).toURL()
                val conn = url.openConnection() as HttpURLConnection
                conn.setRequestProperty("Authorization", "Bearer ${credentials.accessToken.tokenValue}")
                conn.requestMethod = "GET"
                conn.connect()

                val responseCode = conn.responseCode
                if (responseCode !in 200..299) {
                    val errorBody = conn.errorStream?.readBytes()?.decodeToString() ?: ""
                    println("Google IAP error: $errorBody")
                    throw BadRequestException("Invalid In-App Receipt")
                }

                val body = conn.inputStream.readBytes().decodeToString()
                json.decodeFromString<SubscriptionPurchaseV2>(body)
            } catch (e: BadRequestException) {
                throw e
            } catch (e: Exception) {
                throw BadRequestException("", cause = e)
            }
        }

    override val environment: GoogleIAPEnvironment = GoogleIAPEnvironment.Live

    context(server: ServerRuntime)
    override suspend fun validateReceiptImplementation(
        userId: Uuid,
        receipts: List<GoogleReceiptValidationRequest>,
    ) {
        val receiptTransactions = receipts.map { it.orderId }
        if (receiptTransactions.isEmpty()) return

        val allExisting = Server.subscriptions.info.table()
            .find(condition { it.transactionId.inside(receiptTransactions) })
            .toList()

        Server.subscriptions.info.table().updateManyIgnoringResult(
            condition = condition {
                Condition.And(
                    listOf(
                        it.user.eq(userId),
                        it.transactionId.notInside(receiptTransactions),
                        it.transactionId.neq(null),
                        it.storeType.eq(StoreType.Google),
                        it.expires.gte(Clock.System.now()),
                    )
                )
            },
            modification = modification { it.expires assign Clock.System.now() }
        )

        receipts.forEach { receipt ->
            val existing = allExisting.find { it.transactionId == receipt.orderId }

            if (existing != null && existing.expires > Clock.System.now() && existing.autoRenewing == receipt.autoRenewing) return@forEach
            val response = getSubPurchase(receipt)

            response.lineItems?.forEach { item ->
                val productId = item.productId ?: return@forEach
                val expires = item.expiryTime ?: return@forEach

                if (existing == null) {
                    Server.products.info.table().get(productId) ?: return@forEach

                    Server.subscriptions.info.table().insertOne(
                        Subscription(
                            user = userId,
                            transactionId = receipt.orderId,
                            product = productId,
                            basePrice = item.offerDetails?.basePlanId,
                            storeType = StoreType.Google,
                            expires = Instant.parse(expires),
                            startTime = Instant.parse(response.startTime!!),
                            autoRenewing = item.autoRenewingPlan?.autoRenewEnabled ?: false
                        )
                    )
                } else {
                    Server.subscriptions.info.table().updateOneByIdIgnoringResult(
                        existing._id,
                        modification {
                            it.expires assign Instant.parse(expires)
                            it.autoRenewing assign (item.autoRenewingPlan?.autoRenewEnabled ?: false)
                        }
                    )
                }
            }
        }
    }

    context(server: ServerRuntime)
    override suspend fun restorePurchaseImplementation(
        userId: Uuid,
        receipts: List<GoogleReceiptValidationRequest>,
    ) {
        val receiptTransactions = receipts.map { it.orderId }
        if (receiptTransactions.isEmpty()) return

        val allExisting = Server.subscriptions.info.table()
            .find(condition { it.transactionId.inside(receiptTransactions) })
            .toList()

        Server.subscriptions.info.table().updateManyIgnoringResult(
            condition = condition {
                Condition.And(
                    listOf(
                        it.user.eq(userId),
                        it.transactionId.notInside(receiptTransactions),
                        it.transactionId.neq(null),
                        it.storeType.eq(StoreType.Google),
                        it.expires.gte(Clock.System.now()),
                    )
                )
            },
            modification = modification { it.expires assign Clock.System.now() }
        )

        receipts.forEach { receipt ->
            val existing = allExisting.find { it.transactionId == receipt.orderId }

            if (existing != null) {
                if (existing.user != userId) {
                    val newSub = existing.copy(
                        _id = Uuid.random(),
                        user = userId,
                        transactionId = receipt.orderId
                    )

                    Server.subscriptions.info.table()
                        .deleteMany(condition { it.transactionId.eq(receipt.orderId) })

                    Server.subscriptions.info.table().insertOne(newSub)
                }
            } else {
                val response = getSubPurchase(receipt)

                response.lineItems?.forEach { item ->
                    val productId = item.productId ?: return@forEach
                    val expires = item.expiryTime ?: return@forEach

                    Server.products.info.table().get(productId) ?: return@forEach

                    Server.subscriptions.info.table().insertOne(
                        Subscription(
                            user = userId,
                            transactionId = receipt.orderId,
                            product = productId,
                            basePrice = item.offerDetails?.basePlanId,
                            storeType = StoreType.Google,
                            expires = Instant.parse(expires),
                            startTime = Instant.parse(response.startTime!!),
                            autoRenewing = item.autoRenewingPlan?.autoRenewEnabled ?: false,
                        )
                    )!!
                }
            }
        }
    }

    enum class AcknowledgementState {
        ACKNOWLEDGEMENT_STATE_UNSPECIFIED,
        ACKNOWLEDGEMENT_STATE_PENDING,
        ACKNOWLEDGEMENT_STATE_ACKNOWLEDGED,
    }

    enum class SubscriptionState {
        SUBSCRIPTION_STATE_UNSPECIFIED,
        SUBSCRIPTION_STATE_PENDING,
        SUBSCRIPTION_STATE_ACTIVE,
        SUBSCRIPTION_STATE_PAUSED,
        SUBSCRIPTION_STATE_IN_GRACE_PERIOD,
        SUBSCRIPTION_STATE_ON_HOLD,
        SUBSCRIPTION_STATE_CANCELED,
        SUBSCRIPTION_STATE_EXPIRED,
        SUBSCRIPTION_STATE_PENDING_PURCHASE_CANCELED,
    }

    enum class PriceChangeMode {
        PRICE_CHANGE_MODE_UNSPECIFIED,
        PRICE_DECREASE,
        PRICE_INCREASE,
        OPT_OUT_PRICE_INCREASE,
    }

    enum class PriceChangeState {
        PRICE_CHANGE_STATE_UNSPECIFIED,
        OUTSTANDING,
        CONFIRMED,
        APPLIED,
    }

    enum class CancelSurveyReason {
        CANCEL_SURVEY_REASON_UNSPECIFIED,
        CANCEL_SURVEY_REASON_NOT_ENOUGH_USAGE,
        CANCEL_SURVEY_REASON_TECHNICAL_ISSUES,
        CANCEL_SURVEY_REASON_COST_RELATED,
        CANCEL_SURVEY_REASON_FOUND_BETTER_APP,
        CANCEL_SURVEY_REASON_OTHERS,
    }

    @Serializable
    data class Money(
        val currencyCode: String,
        val units: String,
        val nanos: Int,
    )

    @Serializable
    data class SubscriptionItemPriceChangeDetails(
        val newPrice: Money? = null,
        val priceChangeMode: PriceChangeMode? = null,
        val priceChangeState: PriceChangeState? = null,
        val expectedNewPriceChargeTime: String? = null,
    )

    @Serializable
    data class PendingCancellation(
        val allowExtendAfterTime: String? = null,
    )

    @Serializable
    data class InstallmentPlan(
        val initialCommittedPaymentsCount: Int? = null,
        val subsequentCommittedPaymentsCount: Int? = null,
        val remainingCommittedPaymentsCount: Int? = null,
        val pendingCancellation: PendingCancellation? = null,
    )

    @Serializable
    data class AutoRenewingPlan(
        val autoRenewEnabled: Boolean = false,
        val recurringPrice: Money? = null,
        val priceChangeDetails: SubscriptionItemPriceChangeDetails? = null,
        val installmentDetails: InstallmentPlan? = null,
    )

    @Serializable
    data class PrepaidPlan(
        val allowExtendAfterTime: String? = null,
    )

    @Serializable
    data class OfferDetails(
        val offerTags: List<String>? = null,
        val basePlanId: String? = null,
        val offerId: String? = null,
    )

    @Serializable
    data class DeferredItemReplacement(
        val productId: String? = null,
    )

    @Serializable
    class OneTimeCode

    @Serializable
    data class VanityCode(
        val promotionCode: String? = null,
    )

    @Serializable
    data class SignupPromotion(
        val oneTimeCode: OneTimeCode? = null,
        val vanityCode: VanityCode? = null,
    )

    @Serializable
    data class SubscriptionPurchaseLineItem(
        val productId: String? = null,
        val expiryTime: String? = null,
        val autoRenewingPlan: AutoRenewingPlan? = null,
        val prepaidPlan: PrepaidPlan? = null,
        val offerDetails: OfferDetails? = null,
        val deferredItemReplacement: DeferredItemReplacement? = null,
        val signupPromotion: SignupPromotion? = null,
    )

    @Serializable
    data class PausedStateContext(
        val autoResumeTime: String? = null,
    )

    @Serializable
    data class CancelSurveyResult(
        val reason: CancelSurveyReason? = null,
        val reasonUserInput: String? = null,
    )

    @Serializable
    data class UserInitiatedCancellation(
        val cancelSurveyResult: CancelSurveyResult? = null,
        val cancelTime: String? = null,
    )

    @Serializable
    class SystemInitiatedCancellation

    @Serializable
    class DeveloperInitiatedCancellation

    @Serializable
    class ReplacementCancellation

    @Serializable
    data class CanceledStateContext(
        val userInitiatedCancellation: UserInitiatedCancellation? = null,
        val systemInitiatedCancellation: SystemInitiatedCancellation? = null,
        val developerInitiatedCancellation: DeveloperInitiatedCancellation? = null,
        val replacementCancellation: ReplacementCancellation? = null,
    )

    @Serializable
    class TestPurchase

    @Serializable
    data class ExternalAccountIdentifiers(
        val externalAccountId: String? = null,
        val obfuscatedExternalAccountId: String? = null,
        val obfuscatedExternalProfileId: String? = null,
    )

    @Serializable
    data class SubscribeWithGoogleInfo(
        val profileId: String? = null,
        val profileName: String? = null,
        val emailAddress: String? = null,
        val givenName: String? = null,
        val familyName: String? = null,
    )

    @Serializable
    data class SubscriptionPurchaseV2(
        val kind: String? = null,
        val regionCode: String? = null,
        val lineItems: List<SubscriptionPurchaseLineItem>? = null,
        val startTime: String? = null,
        val subscriptionState: SubscriptionState? = null,
        val latestOrderId: String? = null,
        val linkedPurchaseToken: String? = null,
        val pausedStateContext: PausedStateContext? = null,
        val canceledStateContext: CanceledStateContext? = null,
        val testPurchase: TestPurchase? = null,
        val acknowledgementState: AcknowledgementState? = null,
        val externalAccountIdentifiers: ExternalAccountIdentifiers? = null,
        val subscribeWithGoogleInfo: SubscribeWithGoogleInfo? = null,
    )
}
