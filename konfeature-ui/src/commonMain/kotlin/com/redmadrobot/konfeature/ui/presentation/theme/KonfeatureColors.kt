package com.redmadrobot.konfeature.ui.presentation.theme

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

@Stable
internal data object BackgroundColors {
    val primary: Color = KonfeatureBaseColors.Purple99
    val secondary: Color = KonfeatureBaseColors.Purple95
    val tertiary: Color = KonfeatureBaseColors.Purple90
}

@Stable
internal data object ButtonColors {
    val primary: Color = KonfeatureBaseColors.Purple40
    val onPrimary: Color = KonfeatureBaseColors.White
    val secondary: Color = KonfeatureBaseColors.Purple90
    val onSecondary: Color = KonfeatureBaseColors.Purple40
    val error: Color = KonfeatureBaseColors.Error40
    val onError: Color = KonfeatureBaseColors.White
}

@Stable
internal data object ContentColors {
    val primary: Color = KonfeatureBaseColors.Neutral10
    val secondary: Color = KonfeatureBaseColors.Neutral30
    val tertiary: Color = KonfeatureBaseColors.Neutral50
    val accent: Color = KonfeatureBaseColors.Purple40
    val error: Color = KonfeatureBaseColors.Error40
    val teal: Color = KonfeatureBaseColors.Teal
}

@Stable
internal data object StrokeColors {
    val primary: Color = KonfeatureBaseColors.Neutral95
    val secondary: Color = KonfeatureBaseColors.Neutral80
}

@Stable
internal data object SurfaceColors {
    val primary: Color = KonfeatureBaseColors.Purple99
    val secondary: Color = KonfeatureBaseColors.Purple95
    val tertiary: Color = KonfeatureBaseColors.Purple90
    val dialog: Color = KonfeatureBaseColors.Neutral99
    val selected: Color = KonfeatureBaseColors.Purple90
}

@Stable
internal data object SourceColors {
    val defaultText: Color = KonfeatureBaseColors.Neutral50
    val defaultBackground: Color = KonfeatureBaseColors.Neutral95
    val debugText: Color = KonfeatureBaseColors.Green
    val debugBackground: Color = KonfeatureBaseColors.GreenLight
    val remoteText: Color = KonfeatureBaseColors.Orange
    val remoteBackground: Color = KonfeatureBaseColors.OrangeLight
}
