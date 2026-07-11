package com.beemee.iap

@Suppress(names = ["EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING"])
actual object CommonSubscriptionManager {
    actual suspend fun retrieveProducts(productIds: List<String>): List<StoreProduct> {
        TODO("Not yet implemented")
    }

    actual suspend fun checkSubscriptions() {
    }

    actual suspend fun restoreSubscriptions() {
    }

    actual suspend fun purchaseSubscription(
        productId: String,
        priceId: String?,
        oldPurchaseToken: String?
    ) {
    }

    actual suspend fun manageSubscription() {
    }
}