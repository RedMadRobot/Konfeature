package com.redmadrobot.konfeature.ui.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redmadrobot.konfeature.FeatureValueSpec
import com.redmadrobot.konfeature.Konfeature
import com.redmadrobot.konfeature.source.FeatureValueSource
import com.redmadrobot.konfeature.ui.KonfeatureDebugStore
import com.redmadrobot.konfeature.ui.presentation.model.KonfeatureAction
import com.redmadrobot.konfeature.ui.presentation.model.KonfeatureItem
import com.redmadrobot.konfeature.ui.presentation.model.KonfeatureValue
import com.redmadrobot.konfeature.ui.presentation.state.KonfeatureDebugViewState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val SEARCH_QUERY_DELAY_MILLIS = 500L

internal class KonfeatureDebugViewModel(
    private val konfeature: Konfeature,
    private val store: KonfeatureDebugStore,
    private val onValueClick: (key: String, value: Any) -> Unit,
) : ViewModel() {

    private val configs: ImmutableMap<String, KonfeatureItem.Config> = buildConfigs(konfeature)

    private val _state = MutableStateFlow(buildInitialState())
    val state: StateFlow<KonfeatureDebugViewState> = _state.asStateFlow()

    init {
        store.values
            .onEach { refreshValues() }
            .launchIn(viewModelScope)

        observeSearchQuery()
    }

    fun onAction(action: KonfeatureAction) {
        when (action) {
            KonfeatureAction.RefreshClick -> refreshValues()
            KonfeatureAction.CollapseAllClick -> collapseAll()
            KonfeatureAction.ResetAllClick -> viewModelScope.launch { store.resetAll() }
            is KonfeatureAction.ConfigHeaderClick -> toggleConfigCollapse(action.configName)
            is KonfeatureAction.SearchQueryChange -> setSearchQuery(action.query)
            is KonfeatureAction.ToggleChange -> viewModelScope.launch {
                store.setValue(action.key, action.checked)
            }
            is KonfeatureAction.ValueClick -> onValueClick(action.key, action.value)
        }
    }

    private fun toggleConfigCollapse(configName: String) {
        _state.update { state ->
            val collapsed = state.collapsedConfigs
            val newCollapsed = if (configName in collapsed) collapsed.remove(configName) else collapsed.add(configName)
            state.copy(collapsedConfigs = newCollapsed)
        }
    }

    private fun collapseAll() {
        _state.update { state -> state.copy(collapsedConfigs = configs.keys.toPersistentSet()) }
    }

    private fun setSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    private fun refreshValues() {
        val values = buildValues(konfeature)
        _state.update { state ->
            state.copy(
                values = values,
                items = buildItems(values),
            )
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        _state
            .map { it.searchQuery }
            .distinctUntilChanged()
            .debounce(SEARCH_QUERY_DELAY_MILLIS)
            .onEach { query ->
                val keys = computeMatchingKeys(_state.value.values, query)
                _state.update { it.copy(matchingKeys = keys) }
            }
            .launchIn(viewModelScope)
    }

    private fun buildInitialState(): KonfeatureDebugViewState {
        val values = buildValues(konfeature)
        return KonfeatureDebugViewState(
            configs = configs,
            values = values,
            items = buildItems(values),
            matchingKeys = values.toMatchingKeys(),
        )
    }

    private fun buildConfigs(konfeature: Konfeature): ImmutableMap<String, KonfeatureItem.Config> {
        return konfeature.spec.associate { spec ->
            spec.name to KonfeatureItem.Config(name = spec.name, description = spec.description)
        }.toImmutableMap()
    }

    private fun buildValues(konfeature: Konfeature): ImmutableList<KonfeatureItem.Value> {
        return konfeature.spec.flatMap { configSpec ->
            configSpec.values.map { valueSpec ->
                createConfigValueItem(
                    configName = configSpec.name,
                    valueSpec = valueSpec,
                    konfeature = konfeature,
                )
            }
        }.toImmutableList()
    }

    private fun buildItems(values: List<KonfeatureItem.Value>): ImmutableList<KonfeatureItem> {
        return values.groupBy { it.configName }
            .flatMap { (configName, groupValues) ->
                val header = configs[configName]?.let { config ->
                    config.copy(overrideCount = groupValues.count { it.isDebugSource })
                }
                listOfNotNull(header) + groupValues
            }
            .toImmutableList()
    }

    private fun createConfigValueItem(
        configName: String,
        valueSpec: FeatureValueSpec<out Any>,
        konfeature: Konfeature,
    ): KonfeatureItem.Value {
        val configValue = konfeature.getValue(valueSpec)
        val source = configValue.source

        return KonfeatureItem.Value(
            key = valueSpec.key,
            value = KonfeatureValue.of(configValue.value),
            configName = configName,
            sourceName = getSourceName(source),
            description = valueSpec.description,
            isDebugSource = isDebugSource(source, valueSpec.key),
            isDefaultSource = source is FeatureValueSource.Default,
        )
    }

    private fun isDebugSource(source: FeatureValueSource, key: String): Boolean {
        return source is FeatureValueSource.Interceptor && store.currentValue(key) != null
    }

    private fun getSourceName(source: FeatureValueSource): String {
        return when (source) {
            FeatureValueSource.Default -> "Default"
            is FeatureValueSource.Interceptor -> source.name
            is FeatureValueSource.Source -> source.name
        }
    }

    private suspend fun computeMatchingKeys(
        values: List<KonfeatureItem.Value>,
        query: String,
    ): ImmutableSet<String> {
        return withContext(Dispatchers.Default) {
            val matching = if (query.isBlank()) values else values.filter { it.key.contains(query, ignoreCase = true) }
            matching.toMatchingKeys()
        }
    }

    private fun List<KonfeatureItem.Value>.toMatchingKeys(): ImmutableSet<String> {
        return flatMapTo(mutableSetOf()) { value ->
            listOf(
                "${KonfeatureItem.ITEM_KEY_PREFIX_CONFIG}${value.configName}",
                "${KonfeatureItem.ITEM_KEY_PREFIX_VALUE}${value.key}",
            )
        }.toImmutableSet()
    }
}
