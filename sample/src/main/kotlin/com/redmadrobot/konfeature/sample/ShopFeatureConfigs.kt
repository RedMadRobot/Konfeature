package com.redmadrobot.konfeature.sample

import com.redmadrobot.konfeature.FeatureConfig
import com.redmadrobot.konfeature.source.SourceSelectionStrategy

/** Everything about how the shop looks. All values may come from any remote source. */
class AppearanceConfig : FeatureConfig(
    name = "appearance",
    description = "Appearance",
) {

    val isNewProductCardEnabled: Boolean by toggle(
        key = "new_product_card_enabled",
        description = "Redesigned product card with a rating and a discount badge",
        defaultValue = false,
        sourceSelectionStrategy = SourceSelectionStrategy.Any,
    )

    val isDarkThemeEnabled: Boolean by toggle(
        key = "dark_theme_enabled",
        description = "Dark color scheme",
        defaultValue = false,
        sourceSelectionStrategy = SourceSelectionStrategy.Any,
    )

    val catalogColumns: Int by value(
        key = "catalog_columns",
        description = "Number of columns in the catalog grid",
        defaultValue = 2,
        sourceSelectionStrategy = SourceSelectionStrategy.Any,
    )

    val promoBannerText: String by value(
        key = "promo_banner_text",
        description = "Text of the promo banner above the catalog, empty hides the banner",
        defaultValue = "",
        sourceSelectionStrategy = SourceSelectionStrategy.Any,
    )
}

/** Checkout rules. Shows how [SourceSelectionStrategy] limits where a value may come from. */
class CheckoutConfig : FeatureConfig(
    name = "checkout",
    description = "Checkout",
) {

    val isSbpPaymentEnabled: Boolean by toggle(
        key = "sbp_payment_enabled",
        description = "Payment via SBP (fast payment system). Controlled by the A/B-testing source only",
        defaultValue = false,
        sourceSelectionStrategy = SourceSelectionStrategy.anyOf(AbTestingSource.NAME),
    )

    // SourceSelectionStrategy.None (the default): not rolled out remotely yet,
    // the only way to try it out is the debug panel.
    val isOneClickBuyEnabled: Boolean by toggle(
        key = "one_click_buy_enabled",
        description = "\"Buy in one click\" button. Still in development, no remote source is consulted",
        defaultValue = false,
    )

    val freeDeliveryThreshold: Long by value(
        key = "free_delivery_threshold",
        description = "Cart total (₽) starting from which delivery is free",
        defaultValue = 5_000L,
        sourceSelectionStrategy = SourceSelectionStrategy.Any,
    )

    val discountPercent: Double by value(
        key = "discount_percent",
        description = "Discount applied to the whole cart, %",
        defaultValue = 0.0,
        sourceSelectionStrategy = SourceSelectionStrategy.Any,
    )
}
