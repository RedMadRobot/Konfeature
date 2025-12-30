package com.redmadrobot.konfeature.source

/**
 * Interface for intercepting and modifying feature configuration values.
 *
 * Interceptors allow runtime modification of configuration values after they
 * have been resolved from sources. This is particularly useful for:
 * - Debug panels that allow overriding values for testing
 * - A/B testing frameworks that modify values based on user segments
 * - Development-time value overrides
 * - Analytics and monitoring of configuration access
 *
 * Interceptors are applied in the order they were registered, and the last
 * interceptor that returns a non-null value wins.
 */
public interface Interceptor {

    /**
     * Unique name identifying this interceptor.
     *
     * The name is used for logging and debugging purposes. It should be
     * descriptive of the interceptor's purpose (e.g., "DebugPanel", "ABTestInterceptor").
     */
    public val name: String

    /**
     * Intercepts a configuration value and optionally provides a replacement.
     *
     * This method is called after a value has been resolved from sources but before
     * it's returned to the caller. The interceptor can examine the value and its
     * source, and optionally return a different value.
     *
     * @param valueSource the source where the value originated from
     * @param key the configuration key
     * @param value the resolved value from the source
     * @return the replacement value, or null to leave the value unchanged
     */
    public fun intercept(valueSource: FeatureValueSource, key: String, value: Any): Any?
}
