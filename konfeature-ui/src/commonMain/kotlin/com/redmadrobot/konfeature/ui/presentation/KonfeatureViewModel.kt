package com.redmadrobot.konfeature.ui.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redmadrobot.konfeature.FeatureValueSpec
import com.redmadrobot.konfeature.Konfeature
import com.redmadrobot.konfeature.source.FeatureValueSource
import com.redmadrobot.konfeature.ui.KonfeatureScreenData
import com.redmadrobot.konfeature.ui.presentation.data.EditDialogState
import com.redmadrobot.konfeature.ui.presentation.data.KonfeatureItem
import com.redmadrobot.konfeature.ui.presentation.data.KonfeatureValue
import com.redmadrobot.konfeature.ui.presentation.data.KonfeatureViewState
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

internal class KonfeatureViewModel(
    private val panel: KonfeatureScreenData,
) : ViewModel() {

    private val configs: Map<String, KonfeatureItem.Config> = buildConfigs(panel.konfeature)

    private val _state = MutableStateFlow(buildInitialState())
    val state: StateFlow<KonfeatureViewState> = _state.asStateFlow()

    init {
        panel.interceptor.valuesFlow
            .onEach { refreshValues() }
            .launchIn(viewModelScope)

        observeSearchQuery()
    }

    fun onValueChanged(key: String, value: KonfeatureValue) {
        viewModelScope.launch {
            panel.interceptor.setValue(key, value.unwrap())
        }
    }

    fun onValueReset(key: String) {
        viewModelScope.launch {
            panel.interceptor.resetValue(key)
        }
    }

    fun onConfigHeaderClick(configName: String) {
        _state.update { state ->
            val collapsed = state.collapsedConfigs
            val newCollapsed = if (configName in collapsed) collapsed - configName else collapsed + configName
            state.copy(collapsedConfigs = newCollapsed)
        }
    }

    fun onRefreshClick() {
        refreshValues()
    }

    fun onResetAllClick() {
        viewModelScope.launch {
            panel.interceptor.resetAllValues()
        }
    }

    fun onCollapseAllClick() {
        _state.update { state -> state.copy(collapsedConfigs = configs.keys) }
    }

    fun onEditClick(key: String, value: KonfeatureValue, isDebugSource: Boolean) {
        _state.update { it.copy(editDialogState = EditDialogState(key, value, isDebugSource)) }
    }

    fun onEditDialogCloseClicked() {
        _state.update { it.copy(editDialogState = null) }
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    private fun refreshValues() {
        val values = buildValues(panel.konfeature)
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

    private fun buildInitialState(): KonfeatureViewState {
        val values = buildValues(panel.konfeature)
        return KonfeatureViewState(
            configs = configs,
            values = values,
            items = buildItems(values),
            matchingKeys = values.toMatchingKeys(),
        )
    }

    private fun buildConfigs(konfeature: Konfeature): Map<String, KonfeatureItem.Config> {
        return konfeature.spec.associate { spec ->
            spec.name to KonfeatureItem.Config(name = spec.name, description = spec.description)
        }
    }

    private fun buildValues(konfeature: Konfeature): List<KonfeatureItem.Value> {
        return konfeature.spec.flatMap { configSpec ->
            configSpec.values.map { valueSpec ->
                createConfigValueItem(
                    configName = configSpec.name,
                    valueSpec = valueSpec,
                    konfeature = konfeature,
                )
            }
        }
    }

    private fun buildItems(values: List<KonfeatureItem.Value>): List<KonfeatureItem> {
        return values.groupBy { it.configName }
            .flatMap { (configName, groupValues) ->
                val header = configs[configName]?.let { config ->
                    config.copy(overrideCount = groupValues.count { it.isDebugSource })
                }
                listOfNotNull(header) + groupValues
            }
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
            isDebugSource = isDebugSource(source),
            isDefaultSource = source is FeatureValueSource.Default,
        )
    }

    private fun isDebugSource(source: FeatureValueSource): Boolean {
        return (source as? FeatureValueSource.Interceptor)?.name == panel.interceptor.name
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
    ): Set<String> {
        return withContext(Dispatchers.Default) {
            val matching = if (query.isBlank()) values else values.filter { it.key.contains(query, ignoreCase = true) }
            matching.toMatchingKeys()
        }
    }

    private fun List<KonfeatureItem.Value>.toMatchingKeys(): Set<String> {
        return flatMapTo(mutableSetOf()) { value ->
            listOf(
                "${KonfeatureItem.ITEM_KEY_PREFIX_CONFIG}${value.configName}",
                "${KonfeatureItem.ITEM_KEY_PREFIX_VALUE}${value.key}",
            )
        }
    }
}
