package com.redmadrobot.konfeature.ui.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import dev.drewhamilton.poko.Poko

/**
 * Semantic color palette for the Konfeature debug panel.
 *
 * These are the only theming tokens the panel reads at runtime, resolved through [KonfeatureTheme].
 * Integrators customize the panel by passing a modified instance to `KonfeatureTheme(colors = ...)`,
 * typically starting from [lightKonfeatureColors] / [darkKonfeatureColors] and overriding individual
 * slots via `copy`.
 *
 * @property background screen background.
 * @property surface elevated surfaces (the search bar).
 * @property surfaceHighlight subtle highlighted surface (override-count badge background).
 * @property stroke borders and dividers, including the toggle track in its off state.
 * @property contentPrimary primary text and icons.
 * @property contentSecondary secondary text (config keys, chip labels).
 * @property contentTertiary tertiary text and icons (hints, descriptions, the default value source).
 * @property accent accent elements: accent text, cursor, override badge, the toggle track when on.
 * @property onAccent content drawn on top of [accent] (the toggle thumb).
 * @property sourceRemote label and value color for values coming from a remote feature source (green).
 * @property sourceDebug label and value color for values overridden via the debug store (red).
 */
@Immutable
@Poko
@Suppress("LongParameterList")
public class KonfeatureColors(
    public val background: Color,
    public val surface: Color,
    public val surfaceHighlight: Color,
    public val stroke: Color,
    public val contentPrimary: Color,
    public val contentSecondary: Color,
    public val contentTertiary: Color,
    public val accent: Color,
    public val onAccent: Color,
    public val sourceRemote: Color,
    public val sourceDebug: Color,
)

/**
 * Default light color palette. Override individual slots via named arguments (or [KonfeatureColors.copy])
 * to brand the panel while keeping the rest of the defaults.
 */
public fun lightKonfeatureColors(
    background: Color = KonfeatureBaseColors.Purple99,
    surface: Color = KonfeatureBaseColors.Purple95,
    surfaceHighlight: Color = KonfeatureBaseColors.Purple90,
    stroke: Color = KonfeatureBaseColors.Neutral95,
    contentPrimary: Color = KonfeatureBaseColors.Neutral10,
    contentSecondary: Color = KonfeatureBaseColors.Neutral30,
    contentTertiary: Color = KonfeatureBaseColors.Neutral50,
    accent: Color = KonfeatureBaseColors.Purple40,
    onAccent: Color = KonfeatureBaseColors.White,
    sourceRemote: Color = KonfeatureBaseColors.Green,
    sourceDebug: Color = KonfeatureBaseColors.Red,
): KonfeatureColors = KonfeatureColors(
    background = background,
    surface = surface,
    surfaceHighlight = surfaceHighlight,
    stroke = stroke,
    contentPrimary = contentPrimary,
    contentSecondary = contentSecondary,
    contentTertiary = contentTertiary,
    accent = accent,
    onAccent = onAccent,
    sourceRemote = sourceRemote,
    sourceDebug = sourceDebug,
)

/**
 * Default dark color palette. Override individual slots via named arguments (or [KonfeatureColors.copy])
 * to brand the panel while keeping the rest of the defaults.
 */
public fun darkKonfeatureColors(
    background: Color = KonfeatureBaseColors.Neutral10,
    surface: Color = KonfeatureBaseColors.NeutralVariant20,
    surfaceHighlight: Color = KonfeatureBaseColors.NeutralVariant30,
    stroke: Color = KonfeatureBaseColors.NeutralVariant40,
    contentPrimary: Color = KonfeatureBaseColors.Neutral90,
    contentSecondary: Color = KonfeatureBaseColors.Neutral80,
    contentTertiary: Color = KonfeatureBaseColors.Neutral60,
    accent: Color = KonfeatureBaseColors.Purple80,
    onAccent: Color = KonfeatureBaseColors.Neutral10,
    sourceRemote: Color = KonfeatureBaseColors.GreenDarkText,
    sourceDebug: Color = KonfeatureBaseColors.RedDarkText,
): KonfeatureColors = KonfeatureColors(
    background = background,
    surface = surface,
    surfaceHighlight = surfaceHighlight,
    stroke = stroke,
    contentPrimary = contentPrimary,
    contentSecondary = contentSecondary,
    contentTertiary = contentTertiary,
    accent = accent,
    onAccent = onAccent,
    sourceRemote = sourceRemote,
    sourceDebug = sourceDebug,
)

/**
 * Maps the semantic palette onto a Material 3 [ColorScheme] so that Material components used inside the
 * panel (ripples, text selection, `IconButton`) follow the same light/dark appearance. Light vs. dark
 * is inferred from the luminance of [background].
 */
internal fun KonfeatureColors.toMaterialColorScheme(isDarkTheme: Boolean): ColorScheme {
    return if (isDarkTheme) {
        darkColorScheme(
            primary = accent,
            onPrimary = onAccent,
            background = background,
            onBackground = contentPrimary,
            surface = surface,
            onSurface = contentPrimary,
            surfaceVariant = surfaceHighlight,
            onSurfaceVariant = contentSecondary,
            outline = stroke,
        )
    } else {
        lightColorScheme(
            primary = accent,
            onPrimary = onAccent,
            background = background,
            onBackground = contentPrimary,
            surface = surface,
            onSurface = contentPrimary,
            surfaceVariant = surfaceHighlight,
            onSurfaceVariant = contentSecondary,
            outline = stroke,
        )
    }
}
