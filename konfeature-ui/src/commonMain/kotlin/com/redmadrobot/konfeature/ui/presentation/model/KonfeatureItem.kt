package com.redmadrobot.konfeature.ui.presentation.model

internal sealed interface KonfeatureItem {
    val itemKey: String

    companion object {
        const val ITEM_KEY_PREFIX_CONFIG = "config_"
        const val ITEM_KEY_PREFIX_VALUE = "value_"
    }

    data class Config(
        val name: String,
        val description: String,
        val overrideCount: Int = 0,
    ) : KonfeatureItem {
        override val itemKey: String
            get() = "${ITEM_KEY_PREFIX_CONFIG}$name"
    }

    data class Value(
        val key: String,
        val configName: String,
        val value: KonfeatureValue,
        val rawValue: Any,
        val description: String,
        val sourceName: String,
        val isDebugSource: Boolean,
        val isDefaultSource: Boolean,
    ) : KonfeatureItem {
        override val itemKey: String
            get() = "${ITEM_KEY_PREFIX_VALUE}$key"
    }
}
