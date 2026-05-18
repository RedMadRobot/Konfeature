package com.redmadrobot.konfeature.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Compose Multiplatform screen that displays all registered feature configs
 * and allows overriding their values at runtime via [DebugPanelInterceptor].
 *
 * @param panel dependencies obtained from [KonfeatureUi.init]
 */
@Composable
public fun KonfeatureDebugScreen(panel: KonfeatureDebugPanel) {
    val viewModel = viewModel { KonfeatureDebugViewModel(panel) }
    // Will be implemented with the debug panel UI
}
