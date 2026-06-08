package com.redmadrobot.konfeature.ui.presentation.state

import com.redmadrobot.konfeature.ui.presentation.model.KonfeatureItem
import com.redmadrobot.konfeature.ui.presentation.model.KonfeatureItem.Companion.ITEM_KEY_PREFIX_VALUE

internal data class KonfeatureDebugViewState(
    val searchQuery: String = "",
    val collapsedConfigs: Set<String> = emptySet(),
    val configs: Map<String, KonfeatureItem.Config> = emptyMap(),
    val values: List<KonfeatureItem.Value> = emptyList(),
    val items: List<KonfeatureItem> = emptyList(),
    val matchingKeys: Set<String> = emptySet(),
) {
    val isSearchActive: Boolean
        get() = searchQuery.isNotBlank()
    val shouldShowEmptySearchItemsHint
        get() = isSearchActive && matchingKeys.none { it.startsWith(ITEM_KEY_PREFIX_VALUE) }
}
