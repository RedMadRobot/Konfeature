package com.redmadrobot.konfeature.ui.presentation.theme

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Stable
internal data object KonfeatureTypography {
    /** Section headers, group names. */
    val titleMedium: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    )

    /** Secondary body text, config values. */
    val bodyMedium: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    )

    /** Technical values (keys, descriptions) — monospace variant of [bodyMedium]. */
    val mono: TextStyle = bodyMedium.copy(fontFamily = FontFamily.Monospace)

    /** Tab labels, chip text, button labels. */
    val labelLarge: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    )

    /** Badges, source indicators. */
    val labelMedium: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    )

    /** Section headers (uppercase), overline text. */
    val labelSmall: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.sp,
    )
}
