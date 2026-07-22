package com.redmadrobot.konfeature.ui.presentation.view

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.redmadrobot.konfeature.ui.presentation.theme.KonfeatureTheme

@Composable
internal fun KonfeatureToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KonfeatureTheme.colors
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedTrackColor = colors.accent,
            checkedThumbColor = colors.onAccent,
            uncheckedTrackColor = colors.stroke,
            uncheckedThumbColor = colors.onAccent,
            uncheckedBorderColor = colors.stroke,
        ),
    )
}
