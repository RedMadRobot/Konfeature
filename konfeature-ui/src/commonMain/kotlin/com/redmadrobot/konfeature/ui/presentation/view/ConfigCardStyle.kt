package com.redmadrobot.konfeature.ui.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

internal val CONFIG_CARD_HORIZONTAL_MARGIN = 12.dp

internal val CONFIG_CARD_CORNER_RADIUS = 12.dp

internal val CONFIG_CARD_BORDER_WIDTH = 1.dp

/**
 * Paints one fragment of a config card: a [fillColor]ed, [shape]d surface with a [borderColor]-colored outline on
 * its outer edges only. The sides are always bordered; [hasBorderTop] / [hasBorderBottom] add the top / bottom
 * edges (used by the header and the last value row). The outline is faked by an underlying border-colored
 * layer that the fill insets away from on the bordered edges, so consecutive fragments stack into one
 * seamless card without internal divider lines.
 */
internal fun Modifier.cardSegment(
    shape: Shape,
    fillColor: Color,
    borderColor: Color,
    hasBorderTop: Boolean,
    hasBorderBottom: Boolean,
): Modifier = this
    .clip(shape = shape)
    .background(color = borderColor)
    .padding(
        start = CONFIG_CARD_BORDER_WIDTH,
        end = CONFIG_CARD_BORDER_WIDTH,
        top = if (hasBorderTop) CONFIG_CARD_BORDER_WIDTH else 0.dp,
        bottom = if (hasBorderBottom) CONFIG_CARD_BORDER_WIDTH else 0.dp,
    )
    .clip(shape = shape)
    .background(color = fillColor)

/**
 * Shape for a config header: the whole card is rounded while collapsed (the header is the card), and
 * only the top corners are rounded while expanded, so it joins seamlessly with the value rows below.
 */
internal fun cardHeaderShape(isCollapsed: Boolean): Shape =
    if (isCollapsed) {
        RoundedCornerShape(size = CONFIG_CARD_CORNER_RADIUS)
    } else {
        RoundedCornerShape(topStart = CONFIG_CARD_CORNER_RADIUS, topEnd = CONFIG_CARD_CORNER_RADIUS)
    }

/**
 * Shape for a value row: the last value of a config rounds off the card's bottom corners; every other
 * row is square so consecutive rows stack into one continuous card.
 */
internal fun cardValueShape(isLast: Boolean): Shape =
    if (isLast) {
        RoundedCornerShape(bottomStart = CONFIG_CARD_CORNER_RADIUS, bottomEnd = CONFIG_CARD_CORNER_RADIUS)
    } else {
        RectangleShape
    }
