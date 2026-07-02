package com.redmadrobot.konfeature.ui.presentation.view

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.redmadrobot.konfeature.ui.presentation.theme.KonfeatureShapes
import com.redmadrobot.konfeature.ui.presentation.theme.KonfeatureTheme
import com.redmadrobot.konfeature.ui.presentation.theme.KonfeatureTypography
import com.redmadrobot.konfeature.ui.resources.Res
import com.redmadrobot.konfeature.ui.resources.icon_clear
import com.redmadrobot.konfeature.ui.resources.icon_search
import org.jetbrains.compose.resources.painterResource

@Suppress("LongMethod")
@Composable
internal fun KonfeatureSearchBar(
    query: String,
    placeholder: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val minHeight by animateDpAsState(
        targetValue = if (query.isNotEmpty()) 48.dp else 40.dp,
        label = "searchBarHeight",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .background(
                color = KonfeatureTheme.colors.surface,
                shape = KonfeatureShapes.medium,
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.icon_search),
            contentDescription = null,
            tint = KonfeatureTheme.colors.contentTertiary,
            modifier = Modifier.size(size = 20.dp),
        )
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(weight = 1f)
                .padding(horizontal = 8.dp),
            textStyle = KonfeatureTypography.mono.copy(color = KonfeatureTheme.colors.contentPrimary),
            singleLine = true,
            cursorBrush = SolidColor(value = KonfeatureTheme.colors.accent),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = KonfeatureTypography.bodyMedium,
                        color = KonfeatureTheme.colors.contentTertiary,
                    )
                }
                innerTextField()
            },
        )
        if (query.isNotEmpty()) {
            IconButton(
                onClick = { onQueryChange("") },
                modifier = Modifier.size(size = 32.dp),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.icon_clear),
                    contentDescription = null,
                    tint = KonfeatureTheme.colors.contentTertiary,
                    modifier = Modifier.size(size = 20.dp),
                )
            }
        }
    }
}
