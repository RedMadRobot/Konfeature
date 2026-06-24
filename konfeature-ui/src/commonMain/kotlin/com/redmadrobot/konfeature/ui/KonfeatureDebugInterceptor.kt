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
        val override = store.currentValue(key) ?: return null
        return coerceToType(override, value).takeIf { it != value }
    }

    /**
     * Aligns the numeric type of [override] with [current].
     *
     * Overrides round-trip through DataStore as JSON, where `Int` is restored as `Long` and `Float`
     * as `Double`. Konfeature resolves values via `defaultValue::class.isInstance(...)`, so a `Long`
     * override for an `Int` config (or a `Double` for a `Float`) would fail the cast and be dropped.
     * Coercing to [current]'s numeric type keeps such overrides effective across restarts.
     */
    private fun coerceToType(override: Any, current: Any): Any {
        if (override::class == current::class || override !is Number || current !is Number) {
            return override
        }
        return when (current) {
            is Int -> override.toInt()
            is Long -> override.toLong()
            is Float -> override.toFloat()
            is Double -> override.toDouble()
            is Short -> override.toShort()
            is Byte -> override.toByte()
            else -> override
        }
    }

    public companion object {
        /** Name reported as [FeatureValueSource.Interceptor.name] for debug overrides. */
        public const val NAME: String = "KonfeatureDebugInterceptor"
    }
}
