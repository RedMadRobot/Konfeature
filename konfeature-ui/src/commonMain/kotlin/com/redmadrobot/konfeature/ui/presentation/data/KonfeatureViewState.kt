package com.redmadrobot.konfeature.ui.presentation.data

private const val ITEM_VALUE_PREFIX_KEY = "value_"

internal data class KonfeatureViewState(
    val searchQuery: String = "",
    val collapsedConfigs: Set<String> = emptySet(),
    val configs: Map<String, KonfeatureItem.Config> = emptyMap(),
    val values: List<KonfeatureItem.Value> = emptyList(),
    val items: List<KonfeatureItem> = emptyList(),
    val matchingKeys: Set<String> = emptySet(),
    val editDialogState: EditDialogState? = null
) {
    val isSearchActive: Boolean
        get() = searchQuery.isNotBlank()
    val shouldShowEmptySearchItemsHint
        get() = isSearchActive && matchingKeys.none { it.startsWith(ITEM_VALUE_PREFIX_KEY) }
}
