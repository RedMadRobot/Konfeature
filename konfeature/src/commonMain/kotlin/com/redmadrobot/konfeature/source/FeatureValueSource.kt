package com.redmadrobot.konfeature.source

import dev.drewhamilton.poko.Poko

/**
 * Sealed class representing the source of a feature configuration value.
 *
 * This class provides information about where a configuration value originated from
 * during the resolution process. It helps with debugging, logging, and understanding
 * the configuration flow.
 */
public sealed class FeatureValueSource {

    /**
     * Indicates the value came from a registered [FeatureSource].
     *
     * @property name the name of the source that provided the value
     */
    @Poko
    public class Source(public val name: String) : FeatureValueSource()

    /**
     * Indicates the value was modified by a registered [Interceptor].
     *
     * @property name the name of the interceptor that provided the value
     */
    @Poko
    public class Interceptor(public val name: String) : FeatureValueSource()

    /**
     * Indicates the value is the default value specified in the [com.redmadrobot.konfeature.FeatureValueSpec].
     *
     * This happens when no sources provide a value or when the source selection
     * strategy excludes all available sources.
     */
    @Suppress("ConvertObjectToDataObject")
    public object Default : FeatureValueSource() {
        override fun toString(): String = "Default"
    }
}
