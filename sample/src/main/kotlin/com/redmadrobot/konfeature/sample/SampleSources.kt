package com.redmadrobot.konfeature.sample

import com.redmadrobot.konfeature.source.FeatureSource

/** Stands in for a remote config service (Firebase Remote Config, an own backend, etc.). */
class RemoteConfigSource : FeatureSource {

    private val store: Map<String, Any> = mapOf(
        "new_product_card_enabled" to true,
        "catalog_columns" to 3,
        "promo_banner_text" to "−20% on all headphones until Sunday",
        "free_delivery_threshold" to 3_000L,
        // Ignored: CheckoutConfig only trusts the A/B-testing source for this key.
        "sbp_payment_enabled" to false,
    )

    override val name: String = NAME

    override fun get(key: String): Any? = store[key]

    companion object {
        const val NAME = "RemoteConfig"
    }
}

/** Stands in for an A/B-testing service that assigned the current user to some experiment groups. */
class AbTestingSource : FeatureSource {

    private val store: Map<String, Any> = mapOf(
        "sbp_payment_enabled" to true,
        // RemoteConfigSource is registered first and also has this key, so its value wins.
        "free_delivery_threshold" to 1_000L,
    )

    override val name: String = NAME

    override fun get(key: String): Any? = store[key]

    companion object {
        const val NAME = "AbTesting"
    }
}
