package com.redmadrobot.konfeature.ui.presentation.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.redmadrobot.konfeature.ui.presentation.model.KonfeatureAction
import com.redmadrobot.konfeature.ui.presentation.model.KonfeatureItem
import com.redmadrobot.konfeature.ui.presentation.theme.KonfeatureShapes
import com.redmadrobot.konfeature.ui.presentation.theme.KonfeatureTheme
import com.redmadrobot.konfeature.ui.presentation.theme.KonfeatureTypography
import com.redmadrobot.konfeature.ui.resources.Res
import com.redmadrobot.konfeature.ui.resources.icon_reset
import com.redmadrobot.konfeature.ui.resources.konfeature_plugin_reset
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ConfigValueItem(
    item: KonfeatureItem.Value,
    onAction: (KonfeatureAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val toggleValue = item.value as? Boolean
    val shape = cardValueShape(isLast = item.isLastInConfig)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = CONFIG_CARD_HORIZONTAL_MARGIN)
            .cardSegment(
                shape = shape,
                fillColor = if (item.isOddRow) {
                    KonfeatureTheme.colors.background
                } else {
                    KonfeatureTheme.colors.surface
                },
                borderColor = KonfeatureTheme.colors.stroke,
                hasBorderTop = false,
                hasBorderBottom = item.isLastInConfig,
            )
            .clickable(
                enabled = toggleValue == null && item.isEditable,
                onClick = { onAction(KonfeatureAction.ValueClick(item)) },
            )
            .padding(start = 16.dp, end = 12.dp)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
    ) {
        ValueInfoColumn(
            item = item,
            modifier = Modifier.weight(weight = VALUE_INFO_WEIGHT),
        )
        ValueColumn(
            item = item,
            toggleValue = toggleValue,
            onAction = onAction,
            modifier = Modifier.weight(weight = VALUE_CONTROL_WEIGHT),
        )
    }
}

@Composable
private fun ValueInfoColumn(
    item: KonfeatureItem.Value,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp),
    ) {
        Text(
            text = item.key,
            style = KonfeatureTypography.monoBold,
            color = KonfeatureTheme.colors.contentPrimary,
        )
        if (item.description.isNotEmpty()) {
            Text(
                text = item.description,
                style = KonfeatureTypography.mono,
                color = KonfeatureTheme.colors.contentTertiary,
            )
        }
        SourceLabel(item = item)
    }
}

@Composable
private fun ValueColumn(
    item: KonfeatureItem.Value,
    toggleValue: Boolean?,
    onAction: (KonfeatureAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
    ) {
        if (toggleValue != null) {
            KonfeatureToggle(
                checked = toggleValue,
                onCheckedChange = { newValue -> onAction(KonfeatureAction.ToggleChange(item.key, newValue)) },
            )
        } else {
            Text(
                text = item.displayValue,
                style = KonfeatureTypography.mono,
                color = KonfeatureTheme.colors.contentPrimary,
                textAlign = TextAlign.End,
            )
        }
        if (item.isDebugSource) {
            ResetButton(
                onClick = { onAction(KonfeatureAction.ResetValueClick(item.key)) },
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun SourceLabel(
    item: KonfeatureItem.Value,
    modifier: Modifier = Modifier,
) {
    Text(
        text = item.sourceName,
        style = KonfeatureTypography.labelMedium,
        color = sourceColor(item = item),
        modifier = modifier,
    )
}

@Composable
private fun ResetButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(shape = KonfeatureShapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 4.dp),
    ) {
        Icon(
            painter = painterResource(Res.drawable.icon_reset),
            contentDescription = null,
            tint = KonfeatureTheme.colors.contentSecondary,
            modifier = Modifier.size(size = 16.dp),
        )
        Text(
            text = stringResource(Res.string.konfeature_plugin_reset),
            style = KonfeatureTypography.labelMedium,
            color = KonfeatureTheme.colors.contentSecondary,
        )
    }
}

@Composable
private fun sourceColor(item: KonfeatureItem.Value): Color = when {
    item.isDebugSource -> KonfeatureTheme.colors.sourceDebug
    !item.isDefaultSource -> KonfeatureTheme.colors.sourceRemote
    else -> KonfeatureTheme.colors.contentTertiary
}

/** Width split of a value row: the key/description/source column vs. the trailing toggle/value column. */
private const val VALUE_INFO_WEIGHT = 0.7f
private const val VALUE_CONTROL_WEIGHT = 0.3f
