package com.redmadrobot.konfeature.ui.presentation.state

import com.redmadrobot.konfeature.ui.presentation.model.KonfeatureConfigGroup
import com.redmadrobot.konfeature.ui.presentation.model.KonfeatureItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList

internal data class KonfeatureDebugViewState(
    val searchQuery: String = "",
    val collapsedConfigs: PersistentSet<String> = persistentSetOf(),
    val groups: ImmutableList<KonfeatureConfigGroup> = persistentListOf(),
    val matchingKeys: ImmutableSet<String> = persistentSetOf(),
) {
    val isSearchActive: Boolean by lazy { searchQuery.isNotBlank() }
    val shouldShowEmptySearchItemsHint by lazy { isSearchActive && matchingKeys.isEmpty() }

    /**
     * The flat list rendered by the debug panel, derived from [groups]. Hidden rows are dropped here
     * rather than rendered at zero height, which avoids performance problems when hiding or showing a
     * large number of elements.
     *
     * While searching, only values matching the query are kept and a config header is shown only when
     * at least one of its values survives; otherwise every value is shown unless its config is
     * collapsed.
     */
    val visibleItems: ImmutableList<KonfeatureItem> by lazy {
        buildList {
            for (group in groups) {
                val visibleValues = if (isSearchActive) {
                    group.values.filter { it.itemKey in matchingKeys }
                } else {
                    group.values
                }
                if (visibleValues.isEmpty()) continue
                add(group.config)
                if (isSearchActive || group.config.name !in collapsedConfigs) {
                    addAll(visibleValues)
                }
            }
        }.toImmutableList()
    }
}
