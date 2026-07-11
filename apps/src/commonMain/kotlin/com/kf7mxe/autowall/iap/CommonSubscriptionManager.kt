package com.beemee.iap

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


data class StorePrice(
    val id: String?,
    val price: Long,
    val formattedPrice: String, // formatted Price
    val cycle: Int?, // Months
    val period: DatePeriod?,
)

data class StoreProduct(
    val id: String,
    val name: String,
    val description: String,
    val prices: List<StorePrice>,
)

@Serializable
data class LocalSubClass(
    val expires: Instant,
    val renews: Boolean,
    val productId: String,
    val baseId: String? = null,
    val transactionId: String,
)

sealed class SubscriptionException(message: String? = null, cause: Throwable? = null): Exception(message, cause)
class AlreadySubscribedException(message: String? = null, cause: Throwable? = null): SubscriptionException(message, cause)
class PriceIdException(message: String? = null, cause: Throwable? = null): SubscriptionException(message, cause)
class OtherPurchaseException(message: String? = null, cause: Throwable? = null): SubscriptionException(message, cause)
object InvalidProductIdException: SubscriptionException("Invalid ProductId")
object SessionException: SubscriptionException("No Authentication Found")

const val subscriptionKey = "CurrentSubscription"

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object CommonSubscriptionManager {
    suspend fun retrieveProducts(productIds: List<String>): List<StoreProduct>
    suspend fun checkSubscriptions()
    suspend fun restoreSubscriptions()
    suspend fun purchaseSubscription(productId: String, priceId:String? = null, oldPurchaseToken: String? = null)
    suspend fun manageSubscription()
}