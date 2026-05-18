package com.redmadrobot.konfeature.ui

import com.redmadrobot.konfeature.Konfeature

/**
 * Service locator that provides dependencies for the Konfeature debug UI.
 *
 * Returns a [KonfeatureDebugPanel] that must be passed to [com.redmadrobot.konfeature.ui.presentation.KonfeatureDebugScreen]:
 *
 * ```kotlin
 * val panel = KonfeatureUi.init(
 *     konfeature = konfeatureInstance,
 *     interceptor = debugPanelInterceptor,
 * )
 *
 * // In navigation:
 * KonfeatureDebugScreen(panel)
 * ```
 */
public object KonfeatureUi {

    /**
     * Creates a [KonfeatureDebugPanel] with the required dependencies.
     *
     * @param konfeature the [Konfeature] instance containing registered configs and sources
     * @param interceptor the [DebugPanelInterceptor] instance that is also registered
     *   with the same [Konfeature] via `addInterceptor`
     * @return a [KonfeatureDebugPanel] to pass to [com.redmadrobot.konfeature.ui.presentation.KonfeatureDebugScreen]
     */
    public fun init(konfeature: Konfeature, interceptor: DebugPanelInterceptor): KonfeatureDebugPanel {
        return KonfeatureDebugPanel(konfeature, interceptor)
    }
}
