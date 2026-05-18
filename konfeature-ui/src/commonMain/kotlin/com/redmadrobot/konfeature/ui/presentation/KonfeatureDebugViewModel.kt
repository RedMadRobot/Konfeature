package com.redmadrobot.konfeature.ui.presentation

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redmadrobot.konfeature.FeatureConfigSpec
import com.redmadrobot.konfeature.FeatureValueSpec
import com.redmadrobot.konfeature.Konfeature
import com.redmadrobot.konfeature.source.FeatureValueSource
import com.redmadrobot.konfeature.ui.KonfeatureDebugPanel
import com.redmadrobot.konfeature.ui.presentation.data.EditDialogState
import com.redmadrobot.konfeature.ui.presentation.data.KonfeatureItem
import com.redmadrobot.konfeature.ui.presentation.data.KonfeatureViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val SEARCH_QUERY_DELAY_MILLIS = 500L

/**
 * State holder for [KonfeatureDebugScreen].
 *
 * Reads registered feature configs via [com.redmadrobot.konfeature.ui.KonfeatureDebugPanel.konfeature] and uses
 * [com.redmadrobot.konfeature.ui.KonfeatureDebugPanel.interceptor] to set/remove runtime overrides.
 */
internal class KonfeatureDebugViewModel(
    private val panel: KonfeatureDebugPanel,
) : ViewModel() {
    private val _state = MutableStateFlow(KonfeatureViewState())
    private val _searchQueryFlow = MutableStateFlow("")

    val state: StateFlow<KonfeatureViewState> = _state.asStateFlow()

    init {
        observeKonfeatureValues()
        observeSearchQuery()
    }

    fun onValueChanged(key: String, value: Any) {
        viewModelScope.launch {
            panel.interceptor.setValue(key, value)
        }
    }

    fun onValueReset(key: String) {
        viewModelScope.launch {
            panel.interceptor.resetValue(key)
        }
    }

    fun onConfigHeaderClick(configName: String) {
        _state.update { state ->
            val newCollapsedConfigs = if (configName in state.collapsedConfigs) {
                state.collapsedConfigs - configName
            } else {
                state.collapsedConfigs + configName
            }
            state.copy(collapsedConfigs = newCollapsedConfigs)
        }
    }

    fun onRefreshClick() {
        viewModelScope.launch { updateItems() }
    }

    fun onResetAllClick() {
        viewModelScope.launch {
            panel.interceptor.resetAllValues()
        }
    }

    fun onCollapseAllClick() {
        _state.update { state -> state.copy(collapsedConfigs = state.configs.keys) }
    }

    fun onEditClick(key: String, value: Any, isDebugSource: Boolean) {
        _state.update { it.copy(editDialogState = EditDialogState(key, value, isDebugSource)) }
    }

    fun onEditDialogCloseClicked() {
        _state.update { it.copy(editDialogState = null) }
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { state -> state.copy(searchQuery = query) }
        _searchQueryFlow.update { query }
    }

    private fun observeKonfeatureValues() {
        panel.interceptor.valuesFlow
            .onEach { updateItems() }
            .launchIn(viewModelScope)
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        _searchQueryFlow
            .debounce(timeoutMillis = SEARCH_QUERY_DELAY_MILLIS)
            .onEach { query ->
                _state.update { state ->
                    state.copy(matchingKeys = computeMatchingKeys(state.values, query))
                }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun updateItems() {
        val (configs, values) = withContext(Dispatchers.IO) { getItems(panel.konfeature) }
        val searchQuery = _searchQueryFlow.value
        val items = buildItems(configs, values)
        val matchingKeys = computeMatchingKeys(values, searchQuery)

        _state.update { state ->
            state.copy(
                configs = configs,
                values = values,
                items = items,
                matchingKeys = matchingKeys,
            )
        }
    }

    private fun buildItems(
        configs: Map<String, KonfeatureItem.Config>,
        values: List<KonfeatureItem.Value>,
    ): List<KonfeatureItem> {
        return buildList {
            var previousValue: KonfeatureItem.Value? = null
            for (value in values) {
                if (previousValue?.configName != value.configName) {
                    configs[value.configName]?.let { config -> add(config) }
                }
                add(value)
                previousValue = value
            }
        }
    }

    private fun getItems(konfeature: Konfeature): Pair<Map<String, KonfeatureItem.Config>, List<KonfeatureItem.Value>> {
        val configs = mutableMapOf<String, KonfeatureItem.Config>()
        val values = mutableListOf<KonfeatureItem.Value>()

        konfeature.spec.fold(configs to values) { acc, configSpec ->
            acc.apply {
                configs[configSpec.name] = createConfigItem(configSpec)
                configSpec.values.mapTo(values) { valueSpec ->
                    createConfigValueItem(
                        configName = configSpec.name,
                        valueSpec = valueSpec,
                        konfeature = konfeature
                    )
                }
            }
        }

        return configs to values
    }

    private fun createConfigItem(config: FeatureConfigSpec): KonfeatureItem.Config {
        return KonfeatureItem.Config(
            name = config.name,
            description = config.description
        )
    }

    private fun createConfigValueItem(
        configName: String,
        valueSpec: FeatureValueSpec<out Any>,
        konfeature: Konfeature,
    ): KonfeatureItem.Value {
        val configValue = konfeature.getValue(valueSpec)

        val sourceColor = when (configValue.source) {
            FeatureValueSource.Default -> Color.Gray
            is FeatureValueSource.Interceptor -> Color.Red
            is FeatureValueSource.Source -> Color.Green
        }

        return KonfeatureItem.Value(
            key = valueSpec.key,
            value = configValue.value,
            configName = configName,
            sourceName = getSourceName(configValue.source),
            sourceColor = sourceColor,
            description = valueSpec.description,
            isDebugSource = isDebugSource(configValue.source),
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
            if (query.isBlank()) {
                values.toMatchingKeys()
            } else {
                val matchingValues = values.filter { it.key.contains(query, ignoreCase = true) }
                matchingValues.toMatchingKeys()
            }
        }
    }

    private fun List<KonfeatureItem.Value>.toMatchingKeys(): Set<String> {
        return this.flatMapTo(destination = mutableSetOf()) { value ->
            listOf("config_${value.configName}", "value_${value.key}")
        }
    }
}
