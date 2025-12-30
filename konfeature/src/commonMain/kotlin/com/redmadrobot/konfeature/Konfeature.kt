package com.redmadrobot.konfeature

/**
 * Main entry point for the Konfeature library that manages feature configuration.
 *
 * Konfeature provides a unified way to work with remote feature configurations by combining
 * multiple data sources, interceptors, and providing runtime access to configuration values.
 *
 * @see FeatureConfig for defining configuration schemas
 * @see com.redmadrobot.konfeature.source.FeatureSource for implementing configuration data sources
 * @see com.redmadrobot.konfeature.source.Interceptor for implementing value overrides
 */
public interface Konfeature {

    /**
     * List of all registered feature configuration specifications.
     *
     * This property provides access to metadata about all registered [FeatureConfig] instances,
     * including their names, descriptions, and value specifications. Useful for building
     * debug panels or configuration management UIs.
     */
    public val spec: List<FeatureConfigSpec>

    /**
     * Retrieves the current value for a given feature configuration specification.
     *
     * The value resolution follows this order:
     * 1. Default value is assigned
     * 2. Sources are filtered using the spec's [com.redmadrobot.konfeature.source.SourceSelectionStrategy]
     * 3. Sources are searched in registration order (first match wins)
     * 4. Interceptors are applied in registration order (last non-null wins)
     *
     * @param T the type of the configuration value
     * @param spec the feature value specification defining the configuration element
     * @return [FeatureValue] containing the resolved value and its source
     */
    public fun <T : Any> getValue(spec: FeatureValueSpec<T>): FeatureValue<T>
}
