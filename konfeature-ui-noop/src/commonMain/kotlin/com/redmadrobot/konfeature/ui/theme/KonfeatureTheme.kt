package com.redmadrobot.konfeature.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalKonfeatureColors = staticCompositionLocalOf<KonfeatureColors?> { null }

/**
 * No-op counterpart of the `konfeature-ui` [KonfeatureTheme], for non-debug builds.
 *
 * [colors] resolves exactly as in `konfeature-ui`, so code reading the palette keeps working.
 */
public object KonfeatureTheme {

    /** The active color palette, provided by the nearest [KonfeatureTheme] wrapper. */
    public val colors: KonfeatureColors
        @Composable
        @ReadOnlyComposable
        get() = LocalKonfeatureColors.current
            ?: error("KonfeatureTheme.colors accessed outside of a KonfeatureTheme { } scope")
}

/**
 * Provides [colors] to [content] and renders it. Unlike `konfeature-ui`, installs no Material theme:
 * there is no panel to style.
 */
@Composable
public fun KonfeatureTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    colors: KonfeatureColors = if (isDarkTheme) darkKonfeatureColors() else lightKonfeatureColors(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalKonfeatureColors provides colors, content = content)
}
