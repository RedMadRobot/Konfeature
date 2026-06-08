package com.redmadrobot.konfeature.ui

import com.redmadrobot.konfeature.source.FeatureValueSource
import com.redmadrobot.konfeature.source.Interceptor

/**
 * An [Interceptor] implementation that overrides feature values at runtime via a debug panel.
 *
 * It is a thin wrapper over a [KonfeatureDebugStore], which owns the overrides and their
 * persistence; this class only answers "which value should be substituted?" on each `getValue`.
 *
 * ```kotlin
 * val store = KonfeatureDebugStore(producePath = { /* ... */ })
 * val interceptor = KonfeatureDebugInterceptor(store)
 *
 * val konfeature = konfeature {
 *     addInterceptor(interceptor)
 *     register(myConfig)
 * }
 * ```
 *
 * Until [KonfeatureDebugStore.load] completes the store reports no overrides, so [intercept]
 * returns `null` and Konfeature yields the source/default value.
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
