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
        override val itemKey: String = "${ITEM_KEY_PREFIX_CONFIG}$name"
    }

    data class Value(
        val key: String,
        val configName: String,
        val displayValue: String,
        val rawValue: Any,
        val defaultValue: Any,
        val isEditable: Boolean,
        val description: String,
        val sourceName: String,
        val isDebugSource: Boolean,
        val isDefaultSource: Boolean,
    ) : KonfeatureItem {
        override val itemKey: String = getItemKey(configName, key)

        companion object {
            /**
             * Builds a stable, collision-free list id for a value.
             *
             * Konfeature guarantees unique config names and unique keys *within* a config, but the same
             * key may appear in different configs. Identifying a value by its key alone would then produce
             * duplicate list keys (a `LazyColumn` crash) and ambiguous lookups, so the id is derived from
             * the `(configName, key)` pair.
             */
            fun getItemKey(configName: String, key: String): String =
                "$ITEM_KEY_PREFIX_VALUE$configName:$key"
        }
    }
}
