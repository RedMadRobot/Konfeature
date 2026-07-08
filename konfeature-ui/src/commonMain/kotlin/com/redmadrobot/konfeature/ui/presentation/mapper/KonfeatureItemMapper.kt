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
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet

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
     * Item keys of the *values* visible for [query]. A blank query matches every value; otherwise the
     * value's key/description and its config's name/description are searched case-insensitively. Header
     * visibility is derived from its values, so config keys are not part of the set.
     */
    fun matchingKeys(
        groups: List<KonfeatureConfigGroup>,
        query: String,
    ): ImmutableSet<String> {
        return groups
            .flatMap { group ->
                if (group.config.matches(query)) group.values else group.values.filter { it.matches(query) }
            }
            .map { it.itemKey }
            .toImmutableSet()
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
