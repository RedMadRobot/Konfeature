package com.redmadrobot.konfeature

import com.redmadrobot.konfeature.source.SourceSelectionStrategy
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

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
