package com.redmadrobot.konfeature.ui.presentation.mapper

import com.redmadrobot.konfeature.FeatureValueSpec
import com.redmadrobot.konfeature.Konfeature
import com.redmadrobot.konfeature.source.FeatureValueSource
import com.redmadrobot.konfeature.ui.KonfeatureDebugInterceptor
import com.redmadrobot.konfeature.ui.KonfeatureValueType
import com.redmadrobot.konfeature.ui.presentation.model.KonfeatureConfigGroup
import com.redmadrobot.konfeature.ui.presentation.model.KonfeatureItem
import com.redmadrobot.konfeature.ui.presentation.model.valueTypeOf
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

internal class KonfeatureItemMapper {

    /** The debug panel's structural source of truth: each config header with its resolved values. */
    fun mapGroups(konfeature: Konfeature): ImmutableList<KonfeatureConfigGroup> {
        return konfeature.spec.map { configSpec ->
            val values = configSpec.values
                .map { valueSpec -> mapValue(configSpec.name, valueSpec, konfeature) }
                .toImmutableList()
            KonfeatureConfigGroup(
                config = KonfeatureItem.Config(
                    name = configSpec.name,
                    description = configSpec.description,
                    overrideCount = values.count { it.isDebugSource },
                ),
                values = values,
            )
        }.toImmutableList()
    }

    /**
     * The flat list rendered by the debug panel, derived from [groups]. Hidden rows are dropped here
     * rather than rendered at zero height, which avoids performance problems when hiding or showing a
     * large number of elements.
     *
     * While searching, only values matching the query are kept and a config header is shown only when
     * at least one of its values survives (a config whose name/description matches keeps all of its
     * values). A group is expanded unless its config is in [collapsedConfigs], both while searching and
     * while browsing — so the user can collapse a config in either mode.
     */
    fun filterItems(
        groups: List<KonfeatureConfigGroup>,
        query: String,
        collapsedConfigs: Set<String>,
    ): ImmutableList<KonfeatureItem> {
        val isSearchActive = query.isNotBlank()
        return buildList {
            for (group in groups) {
                val visibleValues = when {
                    !isSearchActive -> group.values
                    group.config.matches(query) -> group.values
                    else -> group.values.filter { it.matches(query) }
                }
                if (visibleValues.isEmpty()) continue
                val isCollapsed = group.config.name in collapsedConfigs
                add(group.config.copy(isCollapsed = isCollapsed))
                if (!isCollapsed) {
                    addAll(visibleValues)
                }
            }
        }.toImmutableList()
    }

    private fun mapValue(
        configName: String,
        valueSpec: FeatureValueSpec<out Any>,
        konfeature: Konfeature,
    ): KonfeatureItem.Value {
        val configValue = konfeature.getValue(valueSpec)
        val source = configValue.source
        val rawValue = configValue.value
        return KonfeatureItem.Value(
            key = valueSpec.key,
            displayValue = formatValue(rawValue),
            value = rawValue,
            defaultValue = valueSpec.defaultValue,
            isEditable = valueTypeOf(rawValue) != KonfeatureValueType.OTHER,
            configName = configName,
            sourceName = source.displayName(),
            description = valueSpec.description,
            isDebugSource = source.isDebugSource(),
            isDefaultSource = source is FeatureValueSource.Default,
        )
    }

    private fun formatValue(value: Any): String {
        return if (value is String) "\"$value\"" else value.toString()
    }

    private fun FeatureValueSource.isDebugSource(): Boolean {
        return this is FeatureValueSource.Interceptor && name == KonfeatureDebugInterceptor.NAME
    }

    private fun FeatureValueSource.displayName(): String = when (this) {
        FeatureValueSource.Default -> "Default"
        is FeatureValueSource.Interceptor -> name
        is FeatureValueSource.Source -> name
    }

    private fun KonfeatureItem.Config.matches(query: String): Boolean =
        name.contains(query, ignoreCase = true) ||
            description.contains(query, ignoreCase = true)

    private fun KonfeatureItem.Value.matches(query: String): Boolean =
        key.contains(query, ignoreCase = true) ||
            description.contains(query, ignoreCase = true)
}
