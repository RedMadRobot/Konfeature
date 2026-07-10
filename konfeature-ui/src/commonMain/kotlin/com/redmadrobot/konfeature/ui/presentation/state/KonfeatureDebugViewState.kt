package com.redmadrobot.konfeature.ui.presentation.state

import com.redmadrobot.konfeature.ui.presentation.model.KonfeatureConfigGroup
import com.redmadrobot.konfeature.ui.presentation.model.KonfeatureItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

/**
 * @property searchQuery Current search text.
 * @property collapsedConfigs Names of configs the user has collapsed (ignored while searching).
 * @property groups Structural source of truth: every config header with its resolved values.
 * @property visibleItems The flat list actually rendered by the panel, derived from [groups],
 *  [searchQuery] and [collapsedConfigs] off the main thread. See
 *  [com.redmadrobot.konfeature.ui.presentation.mapper.KonfeatureItemMapper.filterItems].
 */
internal data class KonfeatureDebugViewState(
    val searchQuery: String = "",
    val collapsedConfigs: PersistentSet<String> = persistentSetOf(),
    val groups: ImmutableList<KonfeatureConfigGroup> = persistentListOf(),
    val visibleItems: ImmutableList<KonfeatureItem> = persistentListOf(),
) {
    val isSearchActive: Boolean by lazy { searchQuery.isNotBlank() }
    val shouldShowEmptySearchItemsHint: Boolean by lazy { isSearchActive && visibleItems.isEmpty() }
}
