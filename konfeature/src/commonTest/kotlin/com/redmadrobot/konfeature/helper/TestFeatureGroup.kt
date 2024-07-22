package com.redmadrobot.konfeature.helper

import com.redmadrobot.konfeature.delegate.FeatureGroup
import com.redmadrobot.konfeature.source.SourceSelectionStrategy

class TestFeatureGroup(
    withDuplicates: Boolean = false,
    cSourceSelectionStrategy: SourceSelectionStrategy = SourceSelectionStrategy.Any,
) : FeatureGroup(
    name = "TestFeatureGroup",
    description = "TestFeatureGroup description",
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
