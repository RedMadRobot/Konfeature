package com.redmadrobot.konfeature.ui

import com.redmadrobot.konfeature.Konfeature

/**
 * Holds the dependencies required by [com.redmadrobot.konfeature.ui.presentation.KonfeatureDebugScreen].
 *
 * Instances can only be obtained via [KonfeatureUi.init], ensuring that the screen
 * cannot be displayed before the dependencies are provided.
 */
public class KonfeatureDebugPanel internal constructor(
    internal val konfeature: Konfeature,
    internal val interceptor: DebugPanelInterceptor,
)
