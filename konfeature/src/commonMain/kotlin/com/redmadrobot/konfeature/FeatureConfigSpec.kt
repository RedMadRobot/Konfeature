package com.redmadrobot.konfeature

/**
 * Interface representing the specification of a feature configuration.
 *
 * This interface provides metadata about a feature configuration including
 * its name, description, and the list of configuration values it contains.
 * It's implemented by [FeatureConfig] and used for introspection and
 * building debug interfaces.
 *
 * @see FeatureConfig for the concrete implementation
 */
public interface FeatureConfigSpec {
    /**
     * Unique name identifying this feature configuration.
     *
     * The name should be descriptive and unique among all registered configurations.
     */
    public val name: String

    /**
     * Human-readable description of what this feature configuration controls.
     *
     * This description helps developers understand the purpose and scope of
     * the configuration when viewing it in debug panels or documentation.
     */
    public val description: String

    /**
     * List of all configuration value specifications contained in this configuration.
     *
     * This provides access to metadata about each individual configuration element,
     * including their keys, descriptions, default values, and source selection strategies.
     */
    public val values: List<FeatureValueSpec<out Any>>
}
