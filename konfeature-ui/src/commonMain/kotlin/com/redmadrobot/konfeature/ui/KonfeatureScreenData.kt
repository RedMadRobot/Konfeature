package com.redmadrobot.konfeature.ui

import com.redmadrobot.konfeature.Konfeature

/**
 * Holds the dependencies required by [com.redmadrobot.konfeature.ui.presentation.KonfeatureScreen].
 *
 * Construct an instance once after the [konfeature] is built and the [interceptor] is registered:
 *
 * ```kotlin
 * val panel = KonfeatureScreenData(konfeature, interceptor)
 *
 * // In navigation:
 * KonfeatureScreen(panel)
 * ```
 */
public class KonfeatureScreenData(
    internal val konfeature: Konfeature,
    internal val interceptor: KonfeatureInterceptor,
)
