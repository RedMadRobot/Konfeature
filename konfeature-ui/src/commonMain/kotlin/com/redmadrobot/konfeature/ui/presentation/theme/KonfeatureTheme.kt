package com.redmadrobot.konfeature.ui.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * `null` until a [KonfeatureTheme] provides a palette. `KonfeatureDebugPanel` installs a default theme
 * when the integrator has not wrapped it in one, so panel code can always assume a non-null value.
 */
internal val LocalKonfeatureColors = staticCompositionLocalOf<KonfeatureColors?> { null }

/**
 * Entry point for the Konfeature debug panel design tokens.
 *
 * Read the active [colors] inside composables (e.g. `KonfeatureTheme.colors.accent`). Typography and
 * shapes are internal and not customizable.
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
 * Applies the Konfeature debug panel theme to [content].
 *
 * Provides [colors] to descendants and installs a Material 3 [MaterialTheme] with a matching color
 * scheme so Material components rendered inside the panel share the same light/dark appearance.
 *
 * @param colors the palette to use. Defaults to [lightKonfeatureColors] / [darkKonfeatureColors] based
 *   on [isSystemInDarkTheme]. Pass a customized instance to brand the panel.
 */
@Composable
public fun KonfeatureTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    colors: KonfeatureColors = if (isDarkTheme) darkKonfeatureColors() else lightKonfeatureColors(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalKonfeatureColors provides colors) {
        MaterialTheme(colorScheme = colors.toMaterialColorScheme(isDarkTheme), content = content)
    }
}
