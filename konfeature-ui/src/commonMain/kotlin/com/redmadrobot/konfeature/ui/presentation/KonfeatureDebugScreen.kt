package com.redmadrobot.konfeature.ui.presentation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.redmadrobot.konfeature.ui.KonfeatureDebugPanel

/**
 * Compose Multiplatform screen that displays all registered feature configs
 * and allows overriding their values at runtime via [com.redmadrobot.konfeature.ui.DebugPanelInterceptor].
 *
 * @param panel dependencies obtained from [com.redmadrobot.konfeature.ui.KonfeatureUi.init]
 */
@Composable
public fun KonfeatureDebugScreen(panel: KonfeatureDebugPanel) {
    val viewModel = viewModel { KonfeatureDebugViewModel(panel) }
    // Will be implemented with the debug panel UI
}
