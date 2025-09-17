package com.redmadrobot.konfeature

import com.redmadrobot.konfeature.source.SourceSelectionStrategy
import dev.drewhamilton.poko.Poko

/**
 * Specification for a feature configuration value element.
 *
 * This class defines the metadata and behavior for a single configuration element
 * within a [FeatureConfig]. It includes the key used to look up values in sources,
 * documentation, fallback behavior, and source selection rules.
 *
 * @param T the type of the configuration value
 * @property key the unique identifier used to retrieve this value from sources
 * @property description human-readable description of what this configuration element controls
 * @property defaultValue the fallback value used when no sources provide a value
 * @property sourceSelectionStrategy strategy for determining which sources can provide values
 */
@Poko
public class FeatureValueSpec<T : Any>(
    public val key: String,
    public val description: String,
    public val defaultValue: T,
    public val sourceSelectionStrategy: SourceSelectionStrategy
)
