package com.redmadrobot.konfeature.ui.presentation.view

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.redmadrobot.konfeature.ui.presentation.model.KonfeatureAction
import com.redmadrobot.konfeature.ui.presentation.theme.KonfeatureShapes
import com.redmadrobot.konfeature.ui.presentation.theme.KonfeatureTheme
import com.redmadrobot.konfeature.ui.presentation.theme.KonfeatureTypography
import com.redmadrobot.konfeature.ui.resources.Res
import com.redmadrobot.konfeature.ui.resources.konfeature_plugin_collapse_all
import com.redmadrobot.konfeature.ui.resources.konfeature_plugin_refresh
import com.redmadrobot.konfeature.ui.resources.konfeature_plugin_reset_all
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ToolbarChips(
    onAction: (KonfeatureAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
    ) {
        ActionChip(
            label = stringResource(Res.string.konfeature_plugin_refresh),
            onClick = { onAction(KonfeatureAction.RefreshClick) },
        )
        ActionChip(
            label = stringResource(Res.string.konfeature_plugin_collapse_all),
            onClick = { onAction(KonfeatureAction.CollapseAllClick) },
        )
        ActionChip(
            label = stringResource(Res.string.konfeature_plugin_reset_all),
            onClick = { onAction(KonfeatureAction.ResetAllClick) },
        )
    }
}

@Composable
private fun ActionChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        style = KonfeatureTypography.labelLarge,
        color = KonfeatureTheme.colors.contentSecondary,
        modifier = modifier
            .clip(shape = KonfeatureShapes.medium)
            .border(
                width = 1.dp,
                color = KonfeatureTheme.colors.stroke,
                shape = KonfeatureShapes.medium,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    )
}
