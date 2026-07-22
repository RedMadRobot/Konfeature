package com.redmadrobot.konfeature.ui.presentation.theme

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Stable
internal data object KonfeatureTypography {
    /** Config group headers. */
    val titleMedium: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    )

    /** Search hint and placeholder text. */
    val bodyMedium: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    )

    /** Technical text: value descriptions and the search input — monospace variant of [bodyMedium]. */
    val mono: TextStyle = bodyMedium.copy(fontFamily = FontFamily.Monospace)

    /** Value keys — bold monospace variant of [mono], so the key stands out from its description. */
    val monoBold: TextStyle = mono.copy(fontWeight = FontWeight.Bold)

    /** Toolbar chip labels. */
    val labelLarge: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    )

    /** Non-boolean config values and their source labels. */
    val labelMedium: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    )

    /** Override-count badge on config group headers. */
    val labelSmall: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.sp,
    )
}
