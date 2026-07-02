package com.redmadrobot.konfeature.ui

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.redmadrobot.konfeature.Konfeature
import com.redmadrobot.konfeature.ui.presentation.KonfeatureDebugViewModel
import com.redmadrobot.konfeature.ui.presentation.model.KonfeatureAction
import com.redmadrobot.konfeature.ui.presentation.model.KonfeatureItem
import com.redmadrobot.konfeature.ui.presentation.state.KonfeatureDebugViewState
import com.redmadrobot.konfeature.ui.presentation.theme.KonfeatureShapes
import com.redmadrobot.konfeature.ui.presentation.theme.KonfeatureTheme
import com.redmadrobot.konfeature.ui.presentation.theme.KonfeatureTypography
import com.redmadrobot.konfeature.ui.presentation.theme.LocalKonfeatureColors
import com.redmadrobot.konfeature.ui.presentation.view.KonfeatureSearchBar
import com.redmadrobot.konfeature.ui.presentation.view.KonfeatureToggle
import com.redmadrobot.konfeature.ui.resources.Res
import com.redmadrobot.konfeature.ui.resources.icon_clear
import com.redmadrobot.konfeature.ui.resources.icon_keyboard_arrow_down
import com.redmadrobot.konfeature.ui.resources.icon_keyboard_arrow_up
import com.redmadrobot.konfeature.ui.resources.konfeature_plugin_collapse_all
import com.redmadrobot.konfeature.ui.resources.konfeature_plugin_refresh
import com.redmadrobot.konfeature.ui.resources.konfeature_plugin_reset_all
import com.redmadrobot.konfeature.ui.resources.konfeature_plugin_search_empty
import com.redmadrobot.konfeature.ui.resources.konfeature_plugin_search_hint
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Compose Multiplatform screen that displays all registered feature configs and allows overriding
 * their values at runtime via [KonfeatureDebugInterceptor].
 *
 * Boolean values are toggled directly in the list. For other editable values the screen does not edit
 * anything in place — instead it invokes [onValueClick] with a [KonfeatureValueInfo] describing the
 * tapped value (its key, declared type, current and default values, and current source), leaving the
 * integrator to decide what to do (open a custom editor pre-filled with the current value, copy to
 * clipboard, etc.) and to apply the result via [KonfeatureDebugStore.setValue].
 *
 * Values of a type the debug store cannot persist ([KonfeatureValueType.OTHER]) are shown read-only:
 * their rows are not clickable and [onValueClick] is never invoked for them, since they could not be
 * overridden anyway.
 *
 * @param konfeature the built [Konfeature] whose configs are displayed. Must be a stable instance for
 *   the lifetime of this composable: the backing [KonfeatureDebugViewModel] is created once and does
 *   not observe changes to this parameter.
 * @param store the [KonfeatureDebugStore] backing the overrides. Same stability requirement as
 *   [konfeature] — pass a remembered/singleton instance, not one recreated on recomposition.
 * @param onValueClick invoked when a non-boolean value row is tapped, with a [KonfeatureValueInfo]
 *   carrying the context needed to build an editor for that value. Unlike [konfeature] and [store],
 *   this callback may change between recompositions; the latest instance is always invoked.
 */
@Composable
public fun KonfeatureDebugPanel(
    konfeature: Konfeature,
    store: KonfeatureDebugStore,
    onValueClick: (value: KonfeatureValueInfo) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val currentOnValueClick = rememberUpdatedState(onValueClick)
    val viewModel = viewModel {
        KonfeatureDebugViewModel(
            konfeature = konfeature,
            store = store,
            onValueClick = { currentOnValueClick.value(it) },
        )
    }
    val state by viewModel.state.collectAsState()

    val layout: @Composable () -> Unit = {
        KonfeatureLayout(
            modifier = modifier,
            state = state,
            onAction = viewModel::onAction,
        )
    }
    if (LocalKonfeatureColors.current == null) {
        KonfeatureTheme { layout() }
    } else {
        layout()
    }
}

@Composable
private fun KonfeatureLayout(
    state: KonfeatureDebugViewState,
    onAction: (KonfeatureAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = KonfeatureTheme.colors.background)
    ) {
        ToolbarChips(onAction = onAction)
        KonfeatureSearchBar(
            query = state.searchQuery,
            onQueryChange = { onAction(KonfeatureAction.SearchQueryChange(it)) },
            placeholder = stringResource(Res.string.konfeature_plugin_search_hint),
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        AnimatedVisibility(visible = state.shouldShowEmptySearchItemsHint) {
            Text(
                text = stringResource(Res.string.konfeature_plugin_search_empty),
                style = KonfeatureTypography.bodyMedium,
                color = KonfeatureTheme.colors.contentTertiary,
                modifier = Modifier.padding(all = 16.dp),
            )
        }
        val visibleItems = remember(state.items, state.matchingKeys, state.collapsedConfigs, state.isSearchActive) {
            state.visibleItems()
        }
        LazyColumn(modifier = Modifier.weight(weight = 1f)) {
            konfeatureItems(
                items = visibleItems,
                isSearchActive = state.isSearchActive,
                collapsedConfigs = state.collapsedConfigs,
                onAction = onAction,
            )
        }
    }
}

private fun LazyListScope.konfeatureItems(
    items: ImmutableList<KonfeatureItem>,
    isSearchActive: Boolean,
    collapsedConfigs: Set<String>,
    onAction: (KonfeatureAction) -> Unit,
) {
    items(
        items = items,
        key = { item -> item.itemKey },
        contentType = { item ->
            when (item) {
                is KonfeatureItem.Config -> "config"
                is KonfeatureItem.Value -> "value"
            }
        },
    ) { item ->
        when (item) {
            is KonfeatureItem.Config -> {
                val isCollapsed = !isSearchActive && item.name in collapsedConfigs
                ConfigGroupHeader(
                    name = item.description.takeIf { it.isNotEmpty() } ?: item.name,
                    overrideCount = item.overrideCount,
                    isCollapsed = isCollapsed,
                    onClick = { onAction(KonfeatureAction.ConfigHeaderClick(item.name)) },
                    modifier = Modifier.animateItem(),
                )
            }

            is KonfeatureItem.Value -> {
                ConfigValueItem(
                    item = item,
                    onAction = onAction,
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
private fun ToolbarChips(
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
            Text(
                text = overrideCount.toString(),
                style = KonfeatureTypography.labelSmall,
                color = KonfeatureTheme.colors.accent,
                modifier = Modifier
                    .background(
                        color = KonfeatureTheme.colors.surfaceHighlight,
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
    onAction: (KonfeatureAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val toggleValue = item.rawValue as? Boolean
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (toggleValue == null && item.isEditable) {
                    Modifier.clickable { onAction(KonfeatureAction.ValueClick(item.key)) }
                } else {
                    Modifier
                }
            )
            .padding(start = 32.dp, end = 8.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
    ) {
        ValueInfoColumn(
            item = item,
            modifier = Modifier.weight(weight = 1f),
        )

        if (item.isDebugSource) {
            Icon(
                painter = painterResource(Res.drawable.icon_clear),
                contentDescription = null,
                tint = KonfeatureTheme.colors.contentTertiary,
                modifier = Modifier
                    .clip(shape = KonfeatureShapes.medium)
                    .clickable { onAction(KonfeatureAction.ResetValueClick(item.key)) }
                    .padding(all = 4.dp)
                    .size(size = 20.dp),
            )
        }

        if (toggleValue != null) {
            KonfeatureToggle(
                checked = toggleValue,
                onCheckedChange = { newValue -> onAction(KonfeatureAction.ToggleChange(item.key, newValue)) },
            )
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
            style = KonfeatureTypography.mono,
            color = KonfeatureTheme.colors.contentSecondary,
        )
        if (item.description.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = item.description,
                style = KonfeatureTypography.mono,
                color = KonfeatureTheme.colors.contentTertiary,
            )
        }
        if (item.rawValue is Boolean) {
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
            text = item.displayValue,
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
private fun SourceLabel(
    source: String,
    isDebug: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = source,
        style = KonfeatureTypography.labelMedium,
        color = if (isDebug) {
            KonfeatureTheme.colors.sourceDebug
        } else {
            KonfeatureTheme.colors.sourceRemote
        },
        modifier = modifier,
    )
}

@Composable
private fun sourceColor(item: KonfeatureItem.Value): Color = when {
    item.isDebugSource -> KonfeatureTheme.colors.sourceDebug
    !item.isDefaultSource -> KonfeatureTheme.colors.sourceRemote
    else -> KonfeatureTheme.colors.contentTertiary
}
