package com.redmadrobot.konfeature.ui.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * Raw color palette backing the default light and dark [KonfeatureColors].
 *
 * These are the primitive tones only; UI code never references them directly — it goes through the
 * semantic [KonfeatureColors] slots resolved via [KonfeatureTheme]. Kept in one place so the two
 * default themes stay in sync.
 */
internal object KonfeatureBaseColors {
    // Purple (accent / surfaces in the light theme)
    val Purple40 = Color(0xFF6750A4)
    val Purple80 = Color(0xFFD0BCFF)
    val Purple90 = Color(0xFFE8DEF8)
    val Purple95 = Color(0xFFF3EDF7)
    val Purple99 = Color(0xFFFEF7FF)

    // Neutral (content / surfaces)
    val Neutral10 = Color(0xFF1C1B1F)
    val Neutral30 = Color(0xFF49454F)
    val Neutral50 = Color(0xFF79747E)
    val Neutral60 = Color(0xFF938F99)
    val Neutral80 = Color(0xFFCAC4D0)
    val Neutral90 = Color(0xFFE6E0E9)
    val Neutral95 = Color(0xFFE7E0EC)

    // Neutral variant (dark surfaces)
    val NeutralVariant20 = Color(0xFF2B2930)
    val NeutralVariant30 = Color(0xFF36343B)
    val NeutralVariant40 = Color(0xFF414046)

    // Source accents (remote / debug indicators)
    val Green = Color(0xFF1B6E2D)
    val GreenDarkText = Color(0xFF7DD99E)
    val Red = Color(0xFFC62828)
    val RedDarkText = Color(0xFFEF9A9A)

    val White = Color(0xFFFFFFFF)
}
