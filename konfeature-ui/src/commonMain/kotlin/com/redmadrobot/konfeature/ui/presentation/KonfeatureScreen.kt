package com.redmadrobot.konfeature.ui.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.redmadrobot.konfeature.ui.KonfeatureScreenData
import com.redmadrobot.konfeature.ui.presentation.data.KonfeatureItem
import com.redmadrobot.konfeature.ui.presentation.data.KonfeatureValue
import com.redmadrobot.konfeature.ui.presentation.data.KonfeatureViewState
import com.redmadrobot.konfeature.ui.presentation.theme.BackgroundColors
import com.redmadrobot.konfeature.ui.presentation.theme.ContentColors
import com.redmadrobot.konfeature.ui.presentation.theme.KonfeatureShapes
import com.redmadrobot.konfeature.ui.presentation.theme.KonfeatureTypography
import com.redmadrobot.konfeature.ui.presentation.theme.SourceColors
import com.redmadrobot.konfeature.ui.presentation.theme.StrokeColors
import com.redmadrobot.konfeature.ui.presentation.theme.SurfaceColors
import com.redmadrobot.konfeature.ui.presentation.view.AnimatedFilterItem
import com.redmadrobot.konfeature.ui.presentation.view.EditConfigValueDialog
import com.redmadrobot.konfeature.ui.presentation.view.KonfeatureSearchBar
import com.redmadrobot.konfeature.ui.presentation.view.KonfeatureToggle
import com.redmadrobot.konfeature.ui.resources.Res
import com.redmadrobot.konfeature.ui.resources.icon_edit
import com.redmadrobot.konfeature.ui.resources.icon_keyboard_arrow_down
import com.redmadrobot.konfeature.ui.resources.icon_keyboard_arrow_up
import com.redmadrobot.konfeature.ui.resources.konfeature_plugin_collapse_all
import com.redmadrobot.konfeature.ui.resources.konfeature_plugin_refresh
import com.redmadrobot.konfeature.ui.resources.konfeature_plugin_reset_all
import com.redmadrobot.konfeature.ui.resources.konfeature_plugin_search_empty
import com.redmadrobot.konfeature.ui.resources.konfeature_plugin_search_hint
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Compose Multiplatform screen that displays all registered feature configs
 * and allows overriding their values at runtime via [com.redmadrobot.konfeature.ui.KonfeatureInterceptor].
 *
 * @param panel dependency holder constructed via [com.redmadrobot.konfeature.ui.KonfeatureScreenData]
 */
@Composable
public fun KonfeatureScreen(
    panel: KonfeatureScreenData,
    modifier: Modifier = Modifier,
) {
    val viewModel = viewModel { KonfeatureViewModel(panel) }
    val state by viewModel.state.collectAsState()

    KonfeatureLayout(
        modifier = modifier,
        state = state,
        onRefreshClick = viewModel::onRefreshClick,
        onResetAllClick = viewModel::onResetAllClick,
        onCollapseAllClick = viewModel::onCollapseAllClick,
        onHeaderClick = viewModel::onConfigHeaderClick,
        onEditClick = viewModel::onEditClick,
        onSearchQueryChange = viewModel::onSearchQueryChanged,
        onBooleanToggle = { key, checked -> viewModel.onValueChanged(key, KonfeatureValue.Bool(checked)) },
    )

    state.editDialogState?.let { dialogState ->
        EditConfigValueDialog(
            state = dialogState,
            onValueChange = viewModel::onValueChanged,
            onValueReset = viewModel::onValueReset,
            onDismissRequest = viewModel::onEditDialogCloseClicked,
        )
    }
}

@Composable
@Suppress("LongParameterList")
private fun KonfeatureLayout(
    state: KonfeatureViewState,
    onEditClick: (key: String, value: KonfeatureValue, isDebugSource: Boolean) -> Unit,
    onRefreshClick: () -> Unit,
    onCollapseAllClick: () -> Unit,
    onResetAllClick: () -> Unit,
    onHeaderClick: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onBooleanToggle: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = BackgroundColors.primary)
    ) {
        ToolbarChips(
            onRefreshClick = onRefreshClick,
            onCollapseAllClick = onCollapseAllClick,
            onResetAllClick = onResetAllClick,
        )
        KonfeatureSearchBar(
            query = state.searchQuery,
            onQueryChange = onSearchQueryChange,
            placeholder = stringResource(Res.string.konfeature_plugin_search_hint),
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        AnimatedVisibility(visible = state.shouldShowEmptySearchItemsHint) {
            Text(
                text = stringResource(Res.string.konfeature_plugin_search_empty),
                style = KonfeatureTypography.bodyMedium,
                color = ContentColors.tertiary,
                modifier = Modifier.padding(all = 16.dp),
            )
        }
        LazyColumn(modifier = Modifier.weight(weight = 1f)) {
            konfeatureItems(
                state = state,
                onHeaderClick = onHeaderClick,
                onEditClick = onEditClick,
                onBooleanToggle = onBooleanToggle
            )
        }
    }
}

private fun LazyListScope.konfeatureItems(
    state: KonfeatureViewState,
    onHeaderClick: (String) -> Unit,
    onEditClick: (key: String, value: KonfeatureValue, isDebugSource: Boolean) -> Unit,
    onBooleanToggle: (String, Boolean) -> Unit,
) {
    items(
        items = state.items,
        key = { item -> item.itemKey },
        contentType = { item ->
            when (item) {
                is KonfeatureItem.Config -> "config"
                is KonfeatureItem.Value -> "value"
            }
        },
    ) { item ->
        val isMatchingFilter = item.itemKey in state.matchingKeys

        when (item) {
            is KonfeatureItem.Config -> {
                val isCollapsed = !state.isSearchActive && item.name in state.collapsedConfigs
                AnimatedFilterItem(visible = isMatchingFilter) {
                    ConfigGroupHeader(
                        name = item.description.takeIf { it.isNotEmpty() } ?: item.name,
                        overrideCount = item.overrideCount,
                        isCollapsed = isCollapsed,
                        onClick = { onHeaderClick(item.name) },
                    )
                }
            }

            is KonfeatureItem.Value -> {
                val isVisible = isMatchingFilter && (state.isSearchActive || item.configName !in state.collapsedConfigs)
                AnimatedFilterItem(visible = isVisible) {
                    ConfigValueItem(
                        item = item,
                        onEditClick = onEditClick,
                        onBooleanToggle = onBooleanToggle,
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolbarChips(
    onRefreshClick: () -> Unit,
    onCollapseAllClick: () -> Unit,
    onResetAllClick: () -> Unit,
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
            onClick = onRefreshClick,
        )
        ActionChip(
            label = stringResource(Res.string.konfeature_plugin_collapse_all),
            onClick = onCollapseAllClick,
        )
        ActionChip(
            label = stringResource(Res.string.konfeature_plugin_reset_all),
            onClick = onResetAllClick,
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
        color = ContentColors.secondary,
        modifier = modifier
            .clip(shape = KonfeatureShapes.medium)
            .border(
                width = 1.dp,
                color = StrokeColors.primary,
                shape = KonfeatureShapes.medium,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    )
}

@Composable
private fun ConfigGroupHeader(
    name: String,
    overrideCount: Int,
    isCollapsed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = KonfeatureShapes.medium)
            .clickable(onClick = onClick)
            .padding(all = 8.dp),
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
            tint = ContentColors.tertiary,
            modifier = Modifier.size(size = 20.dp),
        )
        Text(
            text = name,
            style = KonfeatureTypography.titleMedium,
            color = ContentColors.primary,
            modifier = Modifier.weight(weight = 1f),
        )
        if (overrideCount > 0) {
            Text(
                text = overrideCount.toString(),
                style = KonfeatureTypography.labelSmall,
                color = ContentColors.accent,
                modifier = Modifier
                    .background(
                        color = SurfaceColors.tertiary,
                        shape = KonfeatureShapes.small,
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun ConfigValueItem(
    item: KonfeatureItem.Value,
    onEditClick: (key: String, value: KonfeatureValue, isDebugSource: Boolean) -> Unit,
    onBooleanToggle: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 32.dp, end = 8.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
    ) {
        ValueInfoColumn(
            item = item,
            modifier = Modifier.weight(weight = 1f),
        )

        when (val value = item.value) {
            is KonfeatureValue.Bool -> KonfeatureToggle(
                checked = value.value,
                onCheckedChange = { newValue -> onBooleanToggle(item.key, newValue) },
            )

            is KonfeatureValue.Int64,
            is KonfeatureValue.Float64,
            is KonfeatureValue.Text -> EditButton(
                onClick = { onEditClick(item.key, value, item.isDebugSource) },
            )

            is KonfeatureValue.Unsupported -> Unit
        }
    }
}

@Composable
private fun ValueInfoColumn(
    item: KonfeatureItem.Value,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = item.key,
            style = KonfeatureTypography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            color = ContentColors.secondary,
        )
        if (item.description.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = item.description,
                style = KonfeatureTypography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                color = ContentColors.tertiary,
            )
        }
        if (item.value is KonfeatureValue.Bool) {
            ValueSourceLabel(item = item, modifier = Modifier.padding(top = 8.dp))
        } else {
            ValueWithSource(item = item)
        }
    }
}

@Composable
private fun ValueWithSource(
    item: KonfeatureItem.Value,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
    ) {
        Text(
            text = formatValue(value = item.value),
            style = KonfeatureTypography.labelMedium,
            color = sourceColor(item = item),
        )
        ValueSourceLabel(item = item)
    }
}

@Composable
private fun ValueSourceLabel(
    item: KonfeatureItem.Value,
    modifier: Modifier = Modifier,
) {
    if (item.isDebugSource || !item.isDefaultSource) {
        SourceLabel(
            source = item.sourceName,
            isDebug = item.isDebugSource,
            modifier = modifier,
        )
    }
}

@Composable
private fun EditButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(size = 32.dp),
    ) {
        Icon(
            painter = painterResource(Res.drawable.icon_edit),
            contentDescription = null,
            tint = ContentColors.accent,
            modifier = Modifier.size(size = 20.dp),
        )
    }
}

@Composable
private fun SourceLabel(
    source: String,
    isDebug: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = source,
        style = KonfeatureTypography.labelMedium,
        color = if (isDebug) {
            ContentColors.teal
        } else {
            SourceColors.remoteText
        },
        modifier = modifier,
    )
}

private fun formatValue(value: KonfeatureValue): String = when (value) {
    is KonfeatureValue.Bool -> value.value.toString()
    is KonfeatureValue.Int64 -> value.value.toString()
    is KonfeatureValue.Float64 -> value.value.toString()
    is KonfeatureValue.Text -> "\"${value.value}\""
    is KonfeatureValue.Unsupported -> value.raw.toString()
}

@Composable
private fun sourceColor(item: KonfeatureItem.Value): Color = when {
    item.isDebugSource -> ContentColors.teal
    !item.isDefaultSource -> SourceColors.remoteText
    else -> ContentColors.tertiary
}
