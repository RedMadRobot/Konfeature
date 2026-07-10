package com.redmadrobot.konfeature.ui.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redmadrobot.konfeature.Konfeature
import com.redmadrobot.konfeature.ui.KonfeatureDebugStore
import com.redmadrobot.konfeature.ui.KonfeatureValueInfo
import com.redmadrobot.konfeature.ui.presentation.mapper.KonfeatureItemMapper
import com.redmadrobot.konfeature.ui.presentation.model.KonfeatureAction
import com.redmadrobot.konfeature.ui.presentation.model.KonfeatureItem
import com.redmadrobot.konfeature.ui.presentation.model.valueTypeOf
import com.redmadrobot.konfeature.ui.presentation.state.*
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val SEARCH_QUERY_DELAY_MILLIS = 500L

internal class KonfeatureDebugViewModel(
    private val konfeature: Konfeature,
    private val store: KonfeatureDebugStore,
    private val mapper: KonfeatureItemMapper = KonfeatureItemMapper(),
    private val onValueClick: (value: KonfeatureValueInfo) -> Unit,
) : ViewModel() {

    private val _state = MutableStateFlow(KonfeatureDebugViewState())
    val state: StateFlow<KonfeatureDebugViewState> = _state.asStateFlow()

    init {
        store.values
            .onEach { recomputeGroups() }
            .launchIn(viewModelScope)

        observeVisibleItems()
    }

    fun onAction(action: KonfeatureAction) {
        when (action) {
            KonfeatureAction.RefreshClick -> viewModelScope.launch { recomputeGroups() }
            KonfeatureAction.CollapseAllClick -> collapseAll()
            KonfeatureAction.ResetAllClick -> viewModelScope.launch { store.resetAll() }
            is KonfeatureAction.ConfigHeaderClick -> toggleConfigCollapse(action.configName)
            is KonfeatureAction.SearchQueryChange -> setSearchQuery(action.query)
            is KonfeatureAction.ToggleChange -> viewModelScope.launch {
                store.setValue(action.key, action.checked)
            }
            is KonfeatureAction.ValueClick -> emitValueClick(action.item)
            is KonfeatureAction.ResetValueClick -> viewModelScope.launch {
                store.resetValue(action.key)
            }
        }
    }

    private fun emitValueClick(item: KonfeatureItem.Value) {
        if (!item.isEditable) return
        onValueClick(
            KonfeatureValueInfo(
                key = item.key,
                configName = item.configName,
                description = item.description,
                type = valueTypeOf(item.defaultValue),
                currentValue = item.value,
                defaultValue = item.defaultValue,
                sourceName = item.sourceName,
                isOverridden = item.isDebugSource,
            )
        )
    }

    private fun toggleConfigCollapse(configName: String) {
        _state.update { state ->
            val newCollapsed = if (configName in state.collapsedConfigs) {
                state.collapsedConfigs.removing(configName)
            } else {
                state.collapsedConfigs.adding(configName)
            }
            state.copy(collapsedConfigs = newCollapsed)
        }
    }

    private fun collapseAll() {
        _state.update { state ->
            state.copy(collapsedConfigs = state.groups.map { it.config.name }.toPersistentSet())
        }
    }

    private fun setSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    /**
     * The single path that recomputes resolved values. The heavy mapping (source resolution per value)
     * runs on [Dispatchers.Default].
     */
    private suspend fun recomputeGroups() = withContext(Dispatchers.Default) {
        val groups = mapper.mapGroups(konfeature)
        _state.update { it.copy(groups = groups) }
    }

    /**
     * Keeps [KonfeatureDebugViewState.visibleItems] the sole derived function of the search query, the
     * current [KonfeatureDebugViewState.groups] and [KonfeatureDebugViewState.collapsedConfigs]. A blank
     * query is applied immediately (no initial flash of an empty list); real typing is debounced.
     * Collapsing and a groups refresh recompute against the current query so the list never lags behind
     * its inputs. Only the query source is debounced, so collapse/expand stays responsive.
     */
    @OptIn(FlowPreview::class)
    private fun observeVisibleItems() {
        val queries = _state
            .map { it.searchQuery }
            .distinctUntilChanged()
            .debounce { query -> if (query.isBlank()) 0L else SEARCH_QUERY_DELAY_MILLIS }

        val groups = _state
            .map { it.groups }
            .distinctUntilChanged()

        val collapsedConfigs = _state
            .map { it.collapsedConfigs }
            .distinctUntilChanged()

        combine(queries, groups, collapsedConfigs) { query, currentGroups, collapsed ->
            mapper.filterItems(currentGroups, query, collapsed)
        }
            .onEach { items -> _state.update { it.copy(visibleItems = items) } }
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }
}
