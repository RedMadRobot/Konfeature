package com.redmadrobot.konfeature.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.redmadrobot.konfeature.Konfeature
import com.redmadrobot.konfeature.ui.presentation.KonfeatureDebugViewModel
import com.redmadrobot.konfeature.ui.presentation.model.KonfeatureAction
import com.redmadrobot.konfeature.ui.presentation.model.KonfeatureItem
import com.redmadrobot.konfeature.ui.presentation.state.KonfeatureDebugViewState
import com.redmadrobot.konfeature.ui.presentation.theme.KonfeatureTheme
import com.redmadrobot.konfeature.ui.presentation.theme.KonfeatureTypography
import com.redmadrobot.konfeature.ui.presentation.theme.LocalKonfeatureColors
import com.redmadrobot.konfeature.ui.presentation.view.ConfigGroupHeader
import com.redmadrobot.konfeature.ui.presentation.view.ConfigValueItem
import com.redmadrobot.konfeature.ui.presentation.view.KonfeatureSearchBar
import com.redmadrobot.konfeature.ui.presentation.view.ToolbarChips
import com.redmadrobot.konfeature.ui.resources.Res
import com.redmadrobot.konfeature.ui.resources.konfeature_plugin_search_empty
import com.redmadrobot.konfeature.ui.resources.konfeature_plugin_search_hint
import kotlinx.collections.immutable.ImmutableList
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
        LazyColumn(
            modifier = Modifier.weight(weight = 1f),
            contentPadding = PaddingValues(bottom = 12.dp),
        ) {
            konfeatureItems(
                items = state.visibleItems,
                onAction = onAction,
            )
        }
    }
}

private fun LazyListScope.konfeatureItems(
    items: ImmutableList<KonfeatureItem>,
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
                ConfigGroupHeader(
                    name = item.description.takeIf { it.isNotEmpty() } ?: item.name,
                    overrideCount = item.overrideCount,
                    isCollapsed = item.isCollapsed,
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
