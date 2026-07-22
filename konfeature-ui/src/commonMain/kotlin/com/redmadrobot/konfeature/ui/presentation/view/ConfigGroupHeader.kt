package com.redmadrobot.konfeature.ui.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.redmadrobot.konfeature.ui.presentation.theme.KonfeatureShapes
import com.redmadrobot.konfeature.ui.presentation.theme.KonfeatureTheme
import com.redmadrobot.konfeature.ui.presentation.theme.KonfeatureTypography
import com.redmadrobot.konfeature.ui.resources.Res
import com.redmadrobot.konfeature.ui.resources.icon_keyboard_arrow_down
import com.redmadrobot.konfeature.ui.resources.icon_keyboard_arrow_up
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun ConfigGroupHeader(
    name: String,
    overrideCount: Int,
    isCollapsed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = cardHeaderShape(isCollapsed = isCollapsed)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = CONFIG_CARD_HORIZONTAL_MARGIN)
            .padding(top = 8.dp)
            .cardSegment(
                shape = shape,
                fillColor = KonfeatureTheme.colors.surface,
                borderColor = KonfeatureTheme.colors.stroke,
                hasBorderTop = true,
                hasBorderBottom = isCollapsed,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 4.dp),
    ) {
        Icon(
            painter = painterResource(
                resource = if (isCollapsed) {
                    Res.drawable.icon_keyboard_arrow_up
                } else {
                    Res.drawable.icon_keyboard_arrow_down
                }
            ),
            contentDescription = null,
            tint = KonfeatureTheme.colors.contentTertiary,
            modifier = Modifier.size(size = 20.dp),
        )
        Text(
            text = name,
            style = KonfeatureTypography.titleMedium,
            color = KonfeatureTheme.colors.contentPrimary,
            modifier = Modifier.weight(weight = 1f),
        )
        if (overrideCount > 0) {
            OverrideCountBadge(count = overrideCount)
        }
    }
}

@Composable
private fun OverrideCountBadge(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = count.toString(),
        style = KonfeatureTypography.labelSmall,
        color = KonfeatureTheme.colors.sourceDebug,
        modifier = modifier
            .background(
                color = KonfeatureTheme.colors.sourceDebug.copy(alpha = 0.12f),
                shape = KonfeatureShapes.small,
            )
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}
