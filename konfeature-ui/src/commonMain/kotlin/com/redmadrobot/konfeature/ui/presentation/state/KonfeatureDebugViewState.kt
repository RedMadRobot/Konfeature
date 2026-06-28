package com.redmadrobot.konfeature.ui.presentation.state

import com.redmadrobot.konfeature.ui.presentation.model.KonfeatureItem
import com.redmadrobot.konfeature.ui.presentation.model.KonfeatureItem.Companion.ITEM_KEY_PREFIX_VALUE
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf

internal data class KonfeatureDebugViewState(
    val searchQuery: String = "",
    val collapsedConfigs: PersistentSet<String> = persistentSetOf(),
    val configs: ImmutableMap<String, KonfeatureItem.Config> = persistentMapOf(),
    val values: ImmutableList<KonfeatureItem.Value> = persistentListOf(),
    val items: ImmutableList<KonfeatureItem> = persistentListOf(),
    val matchingKeys: ImmutableSet<String> = persistentSetOf(),
) {
    val isSearchActive: Boolean
        get() = searchQuery.isNotBlank()
    val shouldShowEmptySearchItemsHint
        get() = isSearchActive && matchingKeys.none { it.startsWith(ITEM_KEY_PREFIX_VALUE) }
}
