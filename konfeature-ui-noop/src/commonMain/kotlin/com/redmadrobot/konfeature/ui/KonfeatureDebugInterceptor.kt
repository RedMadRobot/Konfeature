package com.redmadrobot.konfeature.ui

import com.redmadrobot.konfeature.source.FeatureValueSource
import com.redmadrobot.konfeature.source.Interceptor

/**
 * No-op counterpart of the `konfeature-ui` [KonfeatureDebugInterceptor], for non-debug builds.
 *
 * @param store unused; accepted only to match the real constructor signature.
 */
public class KonfeatureDebugInterceptor(
    store: KonfeatureDebugStore,
) : Interceptor {

    override val name: String = NAME

    override fun intercept(valueSource: FeatureValueSource, key: String, value: Any): Any? = null

    public companion object {
        /** Name reported as [FeatureValueSource.Interceptor.name] for debug overrides. */
        public const val NAME: String = "KonfeatureDebugInterceptor"
    }
}
