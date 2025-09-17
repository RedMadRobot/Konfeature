package com.redmadrobot.konfeature

import com.redmadrobot.konfeature.source.FeatureValueSource
import dev.drewhamilton.poko.Poko

/**
 * Represents a resolved configuration value along with information about its source.
 *
 * This class encapsulates both the actual configuration value and metadata about
 * where the value came from (default, source, or interceptor). This information
 * is useful for debugging, logging, and understanding the configuration resolution flow.
 *
 * @param T the type of the configuration value
 * @property source information about where this value originated from
 * @property value the resolved configuration value
 */
@Poko
public class FeatureValue<T>(
    public val source: FeatureValueSource,
    public val value: T,
)
