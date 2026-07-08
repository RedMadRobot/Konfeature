package com.redmadrobot.konfeature.ui

import dev.drewhamilton.poko.Poko

/**
 * Enriched description of a single non-boolean feature value, handed to
 * [KonfeatureDebugPanel]'s `onValueClick`.
 *
 * It carries everything an integrator needs to build a value editor.
 *
 * @property key the value's key, used to apply an override via [KonfeatureDebugStore.setValue].
 * @property configName name of the [com.redmadrobot.konfeature.FeatureConfig] that declares the value.
 * @property description human-readable description from the value's spec (may be empty).
 * @property type the expected type, derived from the spec's default value.
 * @property currentValue the currently resolved value.
 * @property defaultValue the spec's default value.
 * @property sourceName name of the source the [currentValue] resolved from (`"Default"`, a remote
 *   source name, or the interceptor name for an active override).
 * @property isOverridden `true` when [currentValue] comes from an active debug override.
 */
@Poko
@Suppress("LongParameterList")
public class KonfeatureValueInfo(
    public val key: String,
    public val configName: String,
    public val description: String,
    public val type: KonfeatureValueType,
    public val currentValue: Any,
    public val defaultValue: Any,
    public val sourceName: String,
    public val isOverridden: Boolean,
)

/**
 * The kind of a feature value, derived from its declared default value. Drives which editor an
 * integrator presents in `onValueClick`.
 */
public enum class KonfeatureValueType {
    BOOLEAN,
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    STRING,

    /** A type Konfeature's debug store cannot persist; editing is unsupported. */
    OTHER,
}
