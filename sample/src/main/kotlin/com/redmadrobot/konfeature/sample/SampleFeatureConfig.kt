package com.redmadrobot.konfeature.sample

import com.redmadrobot.konfeature.FeatureConfig
import com.redmadrobot.konfeature.source.SourceSelectionStrategy

class SampleFeatureConfig : FeatureConfig(
    name = "Sample",
    description = "simple sample set"
) {

    val isFeature1Enabled: Boolean by toggle(
        key = "feature1",
        description = "feature1 desc",
        defaultValue = false,
    )

    val isFeature2Enabled: Boolean by toggle(
        key = "feature2",
        description = "feature2 desc",
        defaultValue = true,
        sourceSelectionStrategy = SourceSelectionStrategy.Any
    )

    val isFeature3Enabled: Boolean by toggle(
        key = "feature3",
        description = "feature3 desc",
        defaultValue = false,
        sourceSelectionStrategy = SourceSelectionStrategy.Any
    )

    val velocity: Long by value(
        key = "velocity_value",
        description = "velocity value",
        defaultValue = 90,
        sourceSelectionStrategy = SourceSelectionStrategy.Any
    )

    val isGroupFeatureEnable: Boolean
        get() = isFeature1Enabled && isFeature3Enabled

    val feature4: String by value(
        key = "feature4",
        description = "feature4 desc",
        defaultValue = "true",
        sourceSelectionStrategy = SourceSelectionStrategy.Any
    )

    enum class PUH { A, B, C }

    private val _puhFeature by value(
        key = "puhFeature",
        description = "puhFeature desc",
        defaultValue = PUH.B.name,
        sourceSelectionStrategy = SourceSelectionStrategy.Any
    )

    val puhFeature: PUH
        get() = _puhFeature.let { PUH.valueOf(it) }
}
