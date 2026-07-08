package com.redmadrobot.konfeature.ui

import com.redmadrobot.konfeature.source.FeatureValueSource
import com.redmadrobot.konfeature.source.Interceptor

/**
 * An [Interceptor] implementation that overrides feature values at runtime via a debug panel.
 *
 * ```kotlin
 * val store = KonfeatureDebugStore.create(path = "...")
 * val interceptor = KonfeatureDebugInterceptor(store)
 *
 * val konfeature = konfeature {
 *     addInterceptor(interceptor)
 * }
 * ```
 *
 * Until the store is loaded (see [KonfeatureDebugStore.create]) it
 * reports no overrides, so [intercept] returns `null` and Konfeature yields the source/default value.
 *
 * @param store holds and persists the overrides this interceptor applies.
 */
public class KonfeatureDebugInterceptor(
    private val store: KonfeatureDebugStore,
) : Interceptor {

    override val name: String = NAME

    override fun intercept(valueSource: FeatureValueSource, key: String, value: Any): Any? {
        // The presence of a store entry — not whether it differs from the resolved value — is what makes
        // this an override. Returning it unconditionally lets Konfeature tag the value as coming from this
        // interceptor, so an override equal to the source/default value is still marked and counted
        // (notably a Boolean toggle set to the value it already had).
        val override = store.currentValue(key) ?: return null
        return coerceToType(override, value)
    }

    /**
     * Aligns the numeric type of [debugValue] with [current].
     *
     * Overrides round-trip through DataStore as JSON, where `Int` is restored as `Long` and `Float`
     * as `Double`. Konfeature resolves values via `defaultValue::class.isInstance(...)`, so a `Long`
     * override for an `Int` config (or a `Double` for a `Float`) would fail the cast and be dropped.
     * Coercing to [current]'s numeric type keeps such overrides effective across restarts.
     */
    private fun coerceToType(debugValue: Any, current: Any): Any {
        if (debugValue::class == current::class || debugValue !is Number || current !is Number) {
            return debugValue
        }
        return when (current) {
            is Int -> debugValue.toInt()
            is Long -> debugValue.toLong()
            is Float -> debugValue.toFloat()
            is Double -> debugValue.toDouble()
            is Short -> debugValue.toShort()
            is Byte -> debugValue.toByte()
            else -> debugValue
        }
    }

    public companion object {
        /** Name reported as [FeatureValueSource.Interceptor.name] for debug overrides. */
        public const val NAME: String = "KonfeatureDebugInterceptor"
    }
}
