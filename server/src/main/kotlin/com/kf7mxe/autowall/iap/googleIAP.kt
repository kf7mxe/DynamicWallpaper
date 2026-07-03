package com.kf7mxe.autowall.iap

import com.kf7mxe.autowall.GoogleIAPEnvironment
import com.kf7mxe.autowall.GoogleSubscriptionReceiptValidationResult
import com.kf7mxe.autowall.ProductEndpoints.bind
import com.kf7mxe.autowall.ProductEndpoints.include
import com.kf7mxe.autowall.ProductEndpoints.path
import com.lightningkite.services.*
import kotlinx.serialization.Serializable
import java.io.File
import kotlin.jvm.JvmInline


import com.kf7mxe.autowall.Server
import com.kf7mxe.autowall.Subscription
import com.kf7mxe.autowall.User
import com.kf7mxe.autowall.UserAuth
import com.kf7mxe.autowall.UserAuth.RoleCache.userRole
import com.kf7mxe.autowall.UserRole
import com.kf7mxe.autowall.user
import com.lightningkite.lightningserver.auth.id
import com.lightningkite.lightningserver.auth.require
import com.lightningkite.lightningserver.definition.builder.ServerBuilder
import com.lightningkite.lightningserver.http.post
import com.lightningkite.lightningserver.typed.ApiHttpHandler
import com.lightningkite.lightningserver.typed.ModelRestEndpoints
import com.lightningkite.lightningserver.typed.auth
import com.lightningkite.lightningserver.typed.modelInfo
import com.lightningkite.services.database.Condition
import com.lightningkite.services.database.ModelPermissions
import com.lightningkite.services.database.condition
import com.lightningkite.services.database.eq
import com.lightningkite.services.database.or
import com.lightningkite.services.database.path
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.server.plugins.BadRequestException
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

import kotlin.compareTo
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes


object GoogleIapEndpoints : ServerBuilder() {


        val validateReceipt = Server.database.modelInfo(
            auth = UserAuth.require(),
            permissions = {
                val admin = if (this.auth.userRole() >= UserRole.ADMIN) Condition.Always else Condition.Never
                val own = condition<GoogleSubscriptionReceiptValidationResult> { it.user eq auth.id }
                ModelPermissions(
                    create = admin,
                    read = own or admin,
                    update = admin,
                    delete = admin
                )
            }
        )

        val rest = path include ModelRestEndpoints(validateReceipt)


    val getEnvironment = basePath.path("environment").post bind ApiHttpHandler(
        summary = "Google IAP Environment",
        auth = ,
        implementation = { _: Unit ->
            Server.settings.googleIap().environment
        }
    )

    val validateReceipt = basePath.path("validate-receipt").post.api(
        summary = "Validate Google Receipt",
        authOptions = authOptions<User>(),
        implementation = { auth, receipts ->
            Server.settings.googleIap().validateReceipt(auth.auth.id, receipts)
        }
    )

    val restorePurchase = basePath.path("restore-purchase").post.api(
        summary = "Restore Google Purchases",
        authOptions = authOptions<User>(),
        implementation = { auth, receipts ->
            Server.settings.googleIap().restorePurchase(auth.auth.id, receipts)
        }
    )
}





public interface GoogleIapService : Service {
    public val environment: GoogleIAPEnvironment

    public suspend fun validateReceipt(userId: String, receipts: List<GoogleReceiptValidationRequest>)
    public suspend fun restorePurchase(userId: String, receipts: List<GoogleReceiptValidationRequest>)

    @Serializable
    @JvmInline
    public value class Settings(
        public val url: String,
    ) : Setting<GoogleIapService> {

        override fun invoke(name: String, context: SettingContext): GoogleIapService {
            return parse(name, url, context)
        }

        public companion object : UrlSettingParser<GoogleIapService>() {
            init {
                register("mock") { name, _, context ->
                    MockGoogleIapService(name, context)
                }
                register("google") { name, url, context ->
                    val raw = url.substringAfter("://")
                    val creds = if (raw.startsWith("{")) raw else File(raw).readText()
                    LiveGoogleIapService(name, context, creds)
                }
            }
        }
    }
}




class MockGoogleIapService(
    override val name: String,
    override val context: SettingContext
) : GoogleIapService {

    override val environment: GoogleIAPEnvironment = GoogleIAPEnvironment.Mock

    // Mocks are always healthy
//    override suspend fun healthCheck(): HealthStatus = HealthStatus(HealthStatus.Level.OK)

    override suspend fun validateReceipt(userId: String, receipts: List<GoogleReceiptValidationRequest>) {
        val receipt = receipts.firstOrNull() ?: return
        val now = context.clock.now()

        Server.subscriptions.info.collection().upsertOne(
            condition { it.user.eq(userId) },
            modification {
                it.product assign receipt.purchaseToken
                it.expires assign now.plus(31.days)
            },
            Subscription(
                user = userId,
                transactionId = null,
                storeType = StoreType.Google,
                product = receipt.purchaseToken,
                expires = now.plus(31.days),
                startTime = now,
                autoRenewing = true,
            )
        )
    }

    override suspend fun restorePurchase(userId: String, receipts: List<GoogleReceiptValidationRequest>) {
        validateReceipt(userId, receipts) // Forwarding to keep snippet dry
    }
}




class LiveGoogleIapService(
    override val name: String,
    override val context: SettingContext,
    jsonCreds: String
) : GoogleIapService {

    override val environment: GoogleIAPEnvironment = GoogleIAPEnvironment.Live

    private val credentials: GoogleCredentials = GoogleCredentials
        .fromStream(jsonCreds.byteInputStream())
        .createScoped("https://www.googleapis.com/auth/androidpublisher")


    // Keep your getSubPurchase() helper strictly private to this class...

    override suspend fun validateReceipt(userId: String, receipts: List<GoogleReceiptValidationRequest>) {
        val now = context.clock.now()
        // ... Paste your exact legacy Live validateReceipt body here,
        // swapping "auth.auth.id" for "userId", and "now()" for "now" ...
    }

    override suspend fun restorePurchase(userId: String, receipts: List<GoogleReceiptValidationRequest>) {
        val now = context.clock.now()
        // ... Paste legacy restore body here ...
    }
}















//@Serializable
//data class GoogleIAPSettings(
//    val url: String,
//) : () -> GoogleApi {
//    companion object : Pluggable<GoogleIAPSettings, GoogleApi>() {
//package com.lightningkite.services
//
//import com.lightningkite.services.data.HealthStatus
//import kotlin.time.Duration
//import kotlin.time.Duration.Companion.minutes
//
///**
// * Base interface for all service abstractions in the library.
// *
// * Service represents any external infrastructure dependency (databases, caches, file systems,
// * email providers, etc.) that an application needs to interact with. Implementations provide
// * concrete connectivity to specific service providers.
// *
// * ## Lifecycle Management
// *
// * Services support explicit lifecycle management through [connect] and [disconnect] methods,
// * which is critical for:
// * - Serverless environments (AWS Lambda, Cloud Functions) where connections may be frozen/resumed
// * - Resource pooling and warm-up during application startup
// * - Graceful shutdown procedures
// *
// * ## Health Monitoring
// *
// * All services must implement [healthCheck] to enable monitoring and alerting systems to
// * verify service availability and performance.
// *
// * ## Naming
// *
// * Each service instance must have a unique [name] that identifies it within the application.
// * This name is used for logging, metrics, and debugging purposes.
// *
// * @see SettingContext for the context passed to all service implementations
// * @see HealthStatus for health check result representation
// */
//public interface Service {
//    /**
//     * Unique identifier for this service instance within the application.
//     *
//     * Used for:
//     * - Logging and debugging to identify which service instance is being referenced
//     * - OpenTelemetry traces and metrics tagging
//     * - Service registry lookups
//     *
//     * Example names: "user-database", "session-cache", "s3-uploads"
//     */
//    public val name: String
//
//    /**
//     * Configuration and runtime context provided when the service was instantiated.
//     *
//     * The context contains:
//     * - [SettingContext.internalSerializersModule]: Serializers for custom types
//     * - [SettingContext.openTelemetry]: Optional telemetry for tracing/metrics
//     * - [SettingContext.clock]: Clock for time-dependent operations (mockable for tests)
//     * - [SettingContext.sharedResources]: Shared connection pools and resources
//     * - [SettingContext.projectName]: Application name for logging
//     * - [SettingContext.publicUrl]: Base URL for generating public links
//     */
//    public val context: SettingContext
//
//    /**
//     * Establishes connection to the underlying service provider.
//     *
//     * This method is **optional** to call - most implementations will lazily connect on first use.
//     * However, calling it explicitly is useful for:
//     * - Pre-warming connections during application startup to reduce initial request latency
//     * - Validating configuration early (fail-fast on misconfiguration)
//     * - Connection pooling initialization
//     *
//     * ## Serverless Environments
//     *
//     * In serverless environments (AWS Lambda, SnapStart), this should be called during
//     * initialization to establish connections that will be frozen with the execution context.
//     *
//     * ## Idempotency
//     *
//     * Implementations should make this method idempotent - calling it multiple times
//     * should not create multiple connections or cause errors.
//     */
//    public suspend fun connect() {}
//
//    /**
//     * Explicitly closes connections to the underlying service provider.
//     *
//     * This method is **optional** to call but is critical for:
//     * - Serverless environments (AWS Lambda) before execution context freezes
//     * - Graceful application shutdown to release resources
//     * - Testing scenarios where services need to be torn down between tests
//     *
//     * ## Serverless Environments
//     *
//     * AWS Lambda and similar platforms freeze the execution context between invocations.
//     * Active network connections can cause issues when thawed. Call [disconnect] before
//     * the handler returns to properly release resources.
//     *
//     * ## Idempotency
//     *
//     * Implementations should make this method idempotent - calling it multiple times
//     * should not cause errors, even if already disconnected.
//     */
//    public suspend fun disconnect() {}
//
//    /**
//     * How often health checks should be performed on this service instance.
//     *
//     * Health monitoring systems should use this value to schedule periodic checks.
//     * Implementations can override this based on service characteristics:
//     * - Fast, reliable services (in-memory caches): Longer intervals
//     * - Critical, external services (databases): Shorter intervals
//     * - Expensive checks: Longer intervals to avoid overhead
//     *
//     * Default: 1 minute
//     */
//    public val healthCheckFrequency: Duration get() = 1.minutes
//
//    /**
//     * Verifies that this service is operational and responds within acceptable time limits.
//     *
//     * Health checks should:
//     * - Execute quickly (typically < 5 seconds)
//     * - Test actual connectivity (not just local state)
//     * - Return [HealthStatus.Level.OK] when fully operational
//     * - Return [HealthStatus.Level.WARNING] for degraded performance
//     * - Return [HealthStatus.Level.ERROR] when non-functional
//     * - Return [HealthStatus.Level.URGENT] for critical failures requiring immediate attention
//     *
//     * ## Implementation Guidelines
//     *
//     * Good health check implementation:
//     * ```kotlin
//     * override suspend fun healthCheck(): HealthStatus = try {
//     *     // Perform lightweight operation that verifies connectivity
//     *     database.runCommand(Document("ping", 1))
//     *     HealthStatus(level = HealthStatus.Level.OK)
//     * } catch (e: Exception) {
//     *     HealthStatus(
//     *         level = HealthStatus.Level.ERROR,
//     *         additionalMessage = e.message
//     *     )
//     * }
//     * ```
//     *
//     * @return Current health status with optional diagnostic message
//     */
//    public suspend fun healthCheck(): HealthStatus
//}
//        init {
//            register("mock") { MockGoogleAPI() }
//            register("google") { (url) ->
//                val raw = url.substringAfter("://")
//
//                val creds = if (raw.startsWith("{")) raw else File(raw).readText()
//
//                LiveGoogleApi(creds)
//            }
//        }
//    }
//
//
//    override fun invoke(): GoogleApi = parse(url.substringBefore("://"), this)
//}
//
//
//abstract class GoogleApi {
//    val basePath get() = ServerPath.root.path("iap/google")
//
//    init {
//        basePath.docName = "googleiap"
//    }
//
//    val validatePath get() = basePath.path("validate-receipt")
//    val restorePath get() = basePath.path("restore-purchase")
//    val environmentPath get() = basePath.path("environment")
//
//    abstract val environment: GoogleIAPEnvironment
//    abstract suspend fun validateReceiptImplementation(
//        auth: AuthAndPathParts<User, TypedServerPath0>,
//        receipts: List<GoogleReceiptValidationRequest>,
//    )
//
//    abstract suspend fun restorePurchaseImplementation(
//        auth: AuthAndPathParts<User, TypedServerPath0>,
//        receipts: List<GoogleReceiptValidationRequest>,
//    )
//
//    val getEnvironment: ApiEndpoint<User, TypedServerPath0, Unit, GoogleIAPEnvironment> = environmentPath.post.api(
//        summary = "Google IAP Environment",
//        description = "Returns the servers Google Environment",
//        authOptions = authOptions<User>(),
//        implementation = { _: Unit -> environment })
//
//    val validateReceipt = validatePath.post.api(
//        summary = "Validate Google Receipt",
//        description = "Validates a users Google IAP store receipt",
//        authOptions = authOptions<User>(),
//        implementation = ::validateReceiptImplementation
//    )
//
//    val restorePurchase = restorePath.post.api(
//        summary = "Restore Google Purchases",
//        description = "Validates a users Google IAP store receipt and will move subscription to authenticated User",
//        authOptions = authOptions<User>(),
//        implementation = ::restorePurchaseImplementation
//    )
//
//}
//
//class MockGoogleAPI : GoogleApi() {
//
//    override val environment: GoogleIAPEnvironment = GoogleIAPEnvironment.Mock
//
//    override suspend fun validateReceiptImplementation(
//        auth: AuthAndPathParts<User, TypedServerPath0>,
//        receipts: List<GoogleReceiptValidationRequest>,
//    ) {
//
//        val receipt = receipts.firstOrNull() ?: return
//
//        Server.subscriptions.info.collection().upsertOne(
//
//            condition { it.user.eq(auth.auth.id) },
//            modification {
//                it.product assign receipt.purchaseToken
//                it.expires assign now().plus(31.days)
//            },
//            Subscription(
//                user = auth.auth.id,
//                transactionId = null,
//                storeType = StoreType.Google,
//                product = receipt.purchaseToken,
//                expires = now().plus(31.days),
//                startTime = now(),
//                autoRenewing = true,
//            )
//        )
//    }
//
//    override suspend fun restorePurchaseImplementation(
//        auth: AuthAndPathParts<User, TypedServerPath0>,
//        receipts: List<GoogleReceiptValidationRequest>,
//    ) {
//        val receipt = receipts.firstOrNull() ?: return
//
//        Server.subscriptions.info.collection().upsertOne(
//            condition { it.user.eq(auth.auth.id) },
//            modification {
//                it.product assign receipt.purchaseToken
//                it.expires assign now().plus(31.days)
//            },
//            Subscription(
//                user = auth.auth.id,
//                transactionId = null,
//                storeType = StoreType.Google,
//                product = receipt.purchaseToken,
//                expires = now().plus(31.days),
//                startTime = now(),
//                autoRenewing = true,
//            )
//        )
//    }
//
//}
//
//
//class LiveGoogleApi(jsonCreds: String) : GoogleApi() {
//
//    private val credentials: GoogleCredentials = GoogleCredentials
//        .fromStream(jsonCreds.byteInputStream())
//        .createScoped("https://www.googleapis.com/auth/androidpublisher")
//
//    suspend fun getSubPurchase(receipt: GoogleReceiptValidationRequest): SubscriptionPurchaseV2 {
//        return try {
//            credentials.refreshIfExpired()
//            val result =
//                client.get(urlString = "https://androidpublisher.googleapis.com/androidpublisher/v3/applications/${receipt.packageName}/purchases/subscriptionsv2/tokens/${receipt.purchaseToken}") {
//                    this.bearerAuth(credentials.accessToken.tokenValue)
//                }
//
//            if (!result.status.isSuccess()){
//                println(result.bodyAsText())
//                throw BadRequestException("Invalid In-App Receipt")
//            }
//
//            result.body<SubscriptionPurchaseV2>()
//
//        } catch (e: Exception) {
//            throw BadRequestException("", cause = e)
//        }
//    }
//
//
//    override val environment: GoogleIAPEnvironment = GoogleIAPEnvironment.Live
//
//
//    override suspend fun validateReceiptImplementation(
//        auth: AuthAndPathParts<User, TypedServerPath0>,
//        receipts: List<GoogleReceiptValidationRequest>,
//    ) {
//
//        val receiptTransactions = receipts.map { it.orderId }
//        if (receiptTransactions.isEmpty()) return
//
//        val allExisting = Server.subscriptions.info.collection()
//            .find(condition { it.transactionId.inside(receiptTransactions) })
//            .toList()
//
//        Server.subscriptions.info.collection().updateManyIgnoringResult(
//            condition = condition {
//                Condition.And(
//                    listOf(
//                        it.user.eq(auth.auth.id),
//                        it.transactionId.notInside(receiptTransactions),
//                        it.transactionId.neq(null),
//                        it.storeType.eq(StoreType.Google),
//                        it.expires.gte(now()),
//                    )
//                )
//            },
//            modification = modification { it.expires assign now() }
//        )
//
//        receipts.forEach { receipt ->
//            val existing = allExisting.find { it.transactionId == receipt.orderId }
//
//            if (existing != null && existing.expires > now() && existing.autoRenewing == receipt.autoRenewing) return@forEach
//            val response = getSubPurchase(receipt)
//
//            response.lineItems?.forEach { item ->
//                val productId = item.productId ?: return@forEach
//                val expires = item.expiryTime ?: return@forEach
//
//                if (existing == null) {
//                    Server.products.info.collection().get(productId) ?: return@forEach
//
//                    Server.subscriptions.info.collection().insertOne(
//                        Subscription(
//                            user = auth.auth.id,
//                            transactionId = receipt.orderId,
//                            product = productId,
//                            basePrice = item.offerDetails?.basePlanId,
//                            storeType = StoreType.Google,
//                            expires = Instant.parse(expires),
//                            startTime = Instant.parse(response.startTime!!),
//                            autoRenewing = item.autoRenewingPlan?.autoRenewEnabled ?: false
//                        )
//                    )
//
//                } else {
//                    Server.subscriptions.info.collection().updateOneByIdIgnoringResult(
//                        existing._id,
//                        modification {
//                            it.expires assign Instant.parse(expires)
//                            it.autoRenewing assign (item.autoRenewingPlan?.autoRenewEnabled ?: false)
//                        }
//                    )
//                }
//            }
//        }
//    }
//
//    override suspend fun restorePurchaseImplementation(
//        auth: AuthAndPathParts<User, TypedServerPath0>,
//        receipts: List<GoogleReceiptValidationRequest>,
//    ) {
//
//        val receiptTransactions = receipts.map { it.orderId }
//        if (receiptTransactions.isEmpty()) return
//
//        val allExisting = Server.subscriptions.info.collection()
//            .find(condition { it.transactionId.inside(receiptTransactions) })
//            .toList()
//
//        Server.subscriptions.info.collection().updateManyIgnoringResult(
//            condition = condition {
//                Condition.And(
//                    listOf(
//                        it.user.eq(auth.auth.id),
//                        it.transactionId.notInside(receiptTransactions),
//                        it.transactionId.neq(null),
//                        it.storeType.eq(StoreType.Google),
//                        it.expires.gte(now()),
//                    )
//                )
//            },
//            modification = modification { it.expires assign now() }
//        )
//
//        receipts.forEach { receipt ->
//            val existing = allExisting.find { it.transactionId == receipt.orderId }
//
//            if (existing != null) {
//                if (existing.user != auth.auth.id) {
//                    val newSub = existing.copy(
//                        _id = UUID.random(),
//                        user = auth.auth.id,
//                        transactionId = receipt.orderId
//                    )
//
//                    Server.subscriptions.info.collection()
//                        .deleteMany(condition { it.transactionId.eq(receipt.orderId) })
//
//                    Server.subscriptions.info.collection().insertOne(newSub)
//                }
//            } else {
//                val response = getSubPurchase(receipt)
//
//                response.lineItems?.forEach { item ->
//                    val productId = item.productId ?: return@forEach
//                    val expires = item.expiryTime ?: return@forEach
//
//                    Server.products.info.collection().get(productId) ?: return@forEach
//
//                    Server.subscriptions.info.collection().insertOne(
//                        Subscription(
//                            user = auth.auth.id,
//                            transactionId = receipt.orderId,
//                            product = productId,
//                            basePrice = item.offerDetails?.basePlanId,
//                            storeType = StoreType.Google,
//                            expires = Instant.parse(expires),
//                            startTime = Instant.parse(response.startTime!!),
//                            autoRenewing = item.autoRenewingPlan?.autoRenewEnabled ?: false,
//                        )
//                    )!!
//
//                }
//            }
//        }
//    }
//
//
//    enum class AcknowledgementState {
//        ACKNOWLEDGEMENT_STATE_UNSPECIFIED,
//        ACKNOWLEDGEMENT_STATE_PENDING,
//        ACKNOWLEDGEMENT_STATE_ACKNOWLEDGED,
//    }
//
//    enum class SubscriptionState {
//        SUBSCRIPTION_STATE_UNSPECIFIED,
//        SUBSCRIPTION_STATE_PENDING,
//        SUBSCRIPTION_STATE_ACTIVE,
//        SUBSCRIPTION_STATE_PAUSED,
//        SUBSCRIPTION_STATE_IN_GRACE_PERIOD,
//        SUBSCRIPTION_STATE_ON_HOLD,
//        SUBSCRIPTION_STATE_CANCELED,
//        SUBSCRIPTION_STATE_EXPIRED,
//        SUBSCRIPTION_STATE_PENDING_PURCHASE_CANCELED,
//    }
//
//    enum class PriceChangeMode {
//        PRICE_CHANGE_MODE_UNSPECIFIED,
//        PRICE_DECREASE,
//        PRICE_INCREASE,
//        OPT_OUT_PRICE_INCREASE,
//    }
//
//    enum class PriceChangeState {
//        PRICE_CHANGE_STATE_UNSPECIFIED,
//        OUTSTANDING,
//        CONFIRMED,
//        APPLIED,
//    }
//
//    enum class CancelSurveyReason {
//        CANCEL_SURVEY_REASON_UNSPECIFIED,
//        CANCEL_SURVEY_REASON_NOT_ENOUGH_USAGE,
//        CANCEL_SURVEY_REASON_TECHNICAL_ISSUES,
//        CANCEL_SURVEY_REASON_COST_RELATED,
//        CANCEL_SURVEY_REASON_FOUND_BETTER_APP,
//        CANCEL_SURVEY_REASON_OTHERS,
//    }
//
//    @Serializable
//    data class Money(
//        val currencyCode: String,
//        val units: String,
//        val nanos: Int,
//    )
//
//
//    @Serializable
//    data class SubscriptionItemPriceChangeDetails(
//        val newPrice: Money? = null,
//        val priceChangeMode: PriceChangeMode? = null,
//        val priceChangeState: PriceChangeState? = null,
//        val expectedNewPriceChargeTime: String? = null,
//    )
//
//    @Serializable
//    data class PendingCancellation(
//        val allowExtendAfterTime: String? = null,
//    )
//
//    @Serializable
//    data class InstallmentPlan(
//        val initialCommittedPaymentsCount: Int? = null,
//        val subsequentCommittedPaymentsCount: Int? = null,
//        val remainingCommittedPaymentsCount: Int? = null,
//        val pendingCancellation: PendingCancellation? = null,
//    )
//
//    @Serializable
//    data class AutoRenewingPlan(
//        val autoRenewEnabled: Boolean = false,
//        val recurringPrice: Money? = null,
//        val priceChangeDetails: SubscriptionItemPriceChangeDetails? = null,
//        val installmentDetails: InstallmentPlan? = null,
//    )
//
//    @Serializable
//    data class PrepaidPlan(
//        val allowExtendAfterTime: String? = null,
//    )
//
//    @Serializable
//    data class OfferDetails(
//        val offerTags: List<String>? = null,
//        val basePlanId: String? = null,
//        val offerId: String? = null,
//    )
//
//    @Serializable
//    data class DeferredItemReplacement(
//        val productId: String? = null,
//    )
//
//    @Serializable
//    class OneTimeCode
//
//    @Serializable
//    data class VanityCode(
//        val promotionCode: String? = null,
//    )
//
//    @Serializable
//    data class SignupPromotion(
//        val oneTimeCode: OneTimeCode? = null,
//        val vanityCode: VanityCode? = null,
//    )
//
//    @Serializable
//    data class SubscriptionPurchaseLineItem(
//        val productId: String? = null,
//        val expiryTime: String? = null,
//        val autoRenewingPlan: AutoRenewingPlan? = null,
//        val prepaidPlan: PrepaidPlan? = null,
//        val offerDetails: OfferDetails? = null,
//        val deferredItemReplacement: DeferredItemReplacement? = null,
//        val signupPromotion: SignupPromotion? = null,
//    )
//
//    @Serializable
//    data class PausedStateContext(
//        val autoResumeTime: String? = null,
//    )
//
//    @Serializable
//    data class CancelSurveyResult(
//        val reason: CancelSurveyReason? = null,
//        val reasonUserInput: String? = null,
//    )
//
//    @Serializable
//    data class UserInitiatedCancellation(
//        val cancelSurveyResult: CancelSurveyResult? = null,
//        val cancelTime: String? = null,
//    )
//
//    @Serializable
//    class SystemInitiatedCancellation
//
//    @Serializable
//    class DeveloperInitiatedCancellation
//
//    @Serializable
//    class ReplacementCancellation
//
//    @Serializable
//    data class CanceledStateContext(
//        val userInitiatedCancellation: UserInitiatedCancellation? = null,
//        val systemInitiatedCancellation: SystemInitiatedCancellation? = null,
//        val developerInitiatedCancellation: DeveloperInitiatedCancellation? = null,
//        val replacementCancellation: ReplacementCancellation? = null,
//    )
//
//    @Serializable
//    class TestPurchase()
//
//    @Serializable
//    data class ExternalAccountIdentifiers(
//        val externalAccountId: String? = null,
//        val obfuscatedExternalAccountId: String? = null,
//        val obfuscatedExternalProfileId: String? = null,
//    )
//
//    @Serializable
//    data class SubscribeWithGoogleInfo(
//        val profileId: String? = null,
//        val profileName: String? = null,
//        val emailAddress: String? = null,
//        val givenName: String? = null,
//        val familyName: String? = null,
//    )
//
//
//    @Serializable
//    data class SubscriptionPurchaseV2(
//        val kind: String? = null,
//        val regionCode: String? = null,
//        val lineItems: List<SubscriptionPurchaseLineItem>? = null,
//        val startTime: String? = null,
//        val subscriptionState: SubscriptionState? = null,
//        val latestOrderId: String? = null,
//        val linkedPurchaseToken: String? = null,
//        val pausedStateContext: PausedStateContext? = null,
//        val canceledStateContext: CanceledStateContext? = null,
//        val testPurchase: TestPurchase? = null,
//        val acknowledgementState: AcknowledgementState? = null,
//        val externalAccountIdentifiers: ExternalAccountIdentifiers? = null,
//        val subscribeWithGoogleInfo: SubscribeWithGoogleInfo? = null,
//    )
//
//}
