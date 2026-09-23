package com.redmadrobot.konfeature.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.redmadrobot.konfeature.Konfeature

/**
 * No-op counterpart of the `konfeature-ui` [KonfeatureDebugPanel], for non-debug builds.
 *
 * Renders nothing and never invokes [onValueClick]. All parameters are accepted only to match the
 * real signature.
 */
@Suppress("UNUSED_PARAMETER")
@Composable
public fun KonfeatureDebugPanel(
    konfeature: Konfeature,
    store: KonfeatureDebugStore,
    onValueClick: (value: KonfeatureValueInfo) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // no-op
}
