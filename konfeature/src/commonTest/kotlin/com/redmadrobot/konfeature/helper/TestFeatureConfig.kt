package com.redmadrobot.konfeature.helper

import com.redmadrobot.konfeature.FeatureConfig
import com.redmadrobot.konfeature.source.SourceSelectionStrategy

class TestFeatureConfig(
    withDuplicates: Boolean = false,
    cSourceSelectionStrategy: SourceSelectionStrategy = SourceSelectionStrategy.Any,
) : FeatureConfig(
    name = "TestFeatureConfig",
    description = "TestFeatureConfig description",
) {
    val a by toggle(
        key = "a",
        description = "feature a desc",
        defaultValue = true,
        sourceSelectionStrategy = SourceSelectionStrategy.Any,
    )

    val b by toggle(
        key = if (withDuplicates) "a" else "b",
        description = "feature b desc",
        defaultValue = true,
        sourceSelectionStrategy = SourceSelectionStrategy.None,
    )

    val c: String by value(
        key = if (withDuplicates) "a" else "c",
        description = "feature c desc",
        defaultValue = "feature c",
        sourceSelectionStrategy = cSourceSelectionStrategy,
    )
}
