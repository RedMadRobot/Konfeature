package com.redmadrobot.konfeature.source

/**
 * Abstraction over a data source for feature configuration values.
 *
 * Feature sources provide the actual configuration values that are used by
 * the Konfeature system. Sources can be remote configuration services
 * (Firebase Remote Config, custom backends), local storage, or any other
 * data provider.
 *
 * Sources are searched in the order they were registered, and the first
 * source that returns a non-null value for a given key wins.
 *
 * Example implementations:
 * - Firebase Remote Config
 * - REST API backend
 * - Local preferences/storage
 * - Environment variables
 */
public interface FeatureSource {

    /**
     * Unique name identifying this source.
     *
     * The name is used for logging, debugging, and source selection strategies.
     * It must be unique among all registered sources.
     */
    public val name: String

    /**
     * Retrieves a configuration value for the given key.
     *
     * @param key the configuration key to look up
     * @return the configuration value, or null if not found or not available
     */
    public fun get(key: String): Any?
}
