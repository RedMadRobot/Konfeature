package com.redmadrobot.konfeature.ui.presentation.model

/**
 * A single row rendered in the debug panel list.
 *
 * The list is a flat sequence that mixes two kinds of rows — a [Config] header followed by the
 * [Value] rows that belong to it.
 *
 * @property itemKey Stable, collision-free id used as the list item key. Its prefix
 *  ([ITEM_KEY_PREFIX_CONFIG] / [ITEM_KEY_PREFIX_VALUE]) also identifies the row kind.
 */
internal sealed interface KonfeatureItem {
    val itemKey: String

    companion object {
        const val ITEM_KEY_PREFIX_CONFIG = "config_"
        const val ITEM_KEY_PREFIX_VALUE = "value_"
    }

    /**
     * Header row for a single `FeatureConfig`, grouping the [Value] rows that follow it.
     *
     * @property name Config name, unique across the [Konfeature] instance.
     * @property description Human-readable config description shown under the name.
     * @property overrideCount Number of values in this config currently overridden via the debug
     *  source; used to render an "overrides applied" badge on the header.
     */
    data class Config(
        val name: String,
        val description: String,
        val overrideCount: Int = 0,
    ) : KonfeatureItem {
        override val itemKey: String = "${ITEM_KEY_PREFIX_CONFIG}$name"
    }

    /**
     * A single feature value row within a [Config].
     *
     * @property key Value key, unique within its config but possibly shared across configs.
     * @property configName Name of the owning config; combined with [key] to build a unique [itemKey].
     * @property displayValue Value formatted for display.
     * @property value Current resolved raw value.
     * @property defaultValue Value's default, used as the baseline when resetting an override.
     * @property isEditable Whether this value's type supports editing from the debug panel.
     * @property description Human-readable value description.
     * @property sourceName Name of the source that resolved the current [value].
     * @property isDebugSource `true` when the current value comes from the debug interceptor
     *  (i.e. it is a debug override).
     * @property isDefaultSource `true` when the current value is the unmodified default.
     */
    data class Value(
        val key: String,
        val configName: String,
        val displayValue: String,
        val value: Any,
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
