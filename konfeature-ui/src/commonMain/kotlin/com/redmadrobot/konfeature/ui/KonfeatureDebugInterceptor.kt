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
        return override.takeIf { it != value }
    }

    public companion object {
        /** Name reported as [FeatureValueSource.Interceptor.name] for debug overrides. */
        public const val NAME: String = "KonfeatureDebugInterceptor"
    }
}
