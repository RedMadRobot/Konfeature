package com.redmadrobot.konfeature

import com.redmadrobot.konfeature.source.SourceSelectionStrategy
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Abstract base class for defining feature configurations.
 *
 * FeatureConfig provides a declarative way to define configuration schemas using
 * property delegates. Each configuration element is defined using either `by toggle()`
 * for Boolean values or `by value()` for other types.
 *
 * Example usage:
 * ```kotlin
 * class MyFeatureConfig : FeatureConfig(
 *     name = "my_feature",
 *     description = "Configuration for my feature"
 * ) {
 *     val isEnabled: Boolean by toggle(
 *         key = "my_feature_enabled",
 *         description = "Enable/disable my feature",
 *         defaultValue = false,
 *         sourceSelectionStrategy = SourceSelectionStrategy.Any
 *     )
 *
 *     val timeout: Long by value(
 *         key = "my_feature_timeout",
 *         description = "Timeout in milliseconds",
 *         defaultValue = 5000L
 *     )
 * }
 * ```
 *
 * @param name unique identifier for this configuration
 * @param description human-readable description of this configuration's purpose
 */
public abstract class FeatureConfig(
    override val name: String,
    override val description: String
) : FeatureConfigSpec {
    private var konfeature: Konfeature? = null
    private val _values = mutableListOf<FeatureValueSpec<out Any>>()

    override val values: List<FeatureValueSpec<out Any>>
        get() = _values.toList()

    internal fun bind(konfeature: Konfeature) {
        this.konfeature = konfeature
    }

    @JvmName("ValueBoolean")
    @Deprecated(
        message = "Use toggle instead",
        replaceWith = ReplaceWith("toggle(key, description, defaultValue, sourceSelectionStrategy)"),
        level = DeprecationLevel.ERROR,
    )
    @Suppress("UNUSED_PARAMETER", "FINAL_UPPER_BOUND")
    public fun <T : Boolean> value(
        key: String,
        description: String,
        defaultValue: T,
        sourceSelectionStrategy: SourceSelectionStrategy = SourceSelectionStrategy.None
    ): ReadOnlyProperty<FeatureConfig?, T> {
        error("Use toggle instead of boolean value")
    }

    /**
     * Creates a configuration value delegate for non-Boolean types.
     *
     * @param T the type of the configuration value
     * @param key unique identifier for this value used in sources
     * @param description human-readable description of this value's purpose
     * @param defaultValue fallback value when no sources provide a value
     * @param sourceSelectionStrategy strategy for selecting which sources can provide values
     * @return a property delegate that resolves the configuration value
     */
    public fun <T : Any> value(
        key: String,
        description: String,
        defaultValue: T,
        sourceSelectionStrategy: SourceSelectionStrategy = SourceSelectionStrategy.None
    ): ReadOnlyProperty<FeatureConfig?, T> {
        return createValue(
            key = key,
            description = description,
            defaultValue = defaultValue,
            sourceSelectionStrategy = sourceSelectionStrategy
        )
    }

    /**
     * Creates a configuration toggle delegate for Boolean values.
     *
     * This method should be used instead of `value()` for Boolean configuration
     * elements to provide better semantic clarity for feature flags and toggles.
     *
     * @param key unique identifier for this toggle used in sources
     * @param description human-readable description of this toggle's purpose
     * @param defaultValue fallback value when no sources provide a value
     * @param sourceSelectionStrategy strategy for selecting which sources can provide values
     * @return a property delegate that resolves the Boolean configuration value
     */
    public fun toggle(
        key: String,
        description: String,
        defaultValue: Boolean,
        sourceSelectionStrategy: SourceSelectionStrategy = SourceSelectionStrategy.None,
    ): ReadOnlyProperty<FeatureConfig?, Boolean> {
        return createValue(
            key = key,
            description = description,
            defaultValue = defaultValue,
            sourceSelectionStrategy = sourceSelectionStrategy
        )
    }

    private fun <T : Any> createValue(
        key: String,
        description: String,
        defaultValue: T,
        sourceSelectionStrategy: SourceSelectionStrategy = SourceSelectionStrategy.None
    ): ReadOnlyProperty<FeatureConfig?, T> {
        val spec = FeatureValueSpec(
            key = key,
            description = description,
            defaultValue = defaultValue,
            sourceSelectionStrategy = sourceSelectionStrategy
        )
        _values.add(spec)
        return Value(spec)
    }

    private class Value<T : Any>(
        private val spec: FeatureValueSpec<T>,
    ) : ReadOnlyProperty<FeatureConfig?, T> {
        override fun getValue(thisRef: FeatureConfig?, property: KProperty<*>): T {
            return checkBinding(thisRef?.konfeature).getValue(spec).value
        }

        private fun checkBinding(konFeature: Konfeature?): Konfeature {
            return checkNotNull(konFeature) { "FeatureConfig is not bound to Konfeature" }
        }
    }
}
