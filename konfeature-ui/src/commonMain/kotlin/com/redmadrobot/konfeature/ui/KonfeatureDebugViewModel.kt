package com.redmadrobot.konfeature.ui

import androidx.lifecycle.ViewModel

/**
 * State holder for [KonfeatureDebugScreen].
 *
 * Reads registered feature configs via [KonfeatureDebugPanel.konfeature] and uses
 * [KonfeatureDebugPanel.interceptor] to set/remove runtime overrides.
 */
internal class KonfeatureDebugViewModel(
    private val panel: KonfeatureDebugPanel,
) : ViewModel()
