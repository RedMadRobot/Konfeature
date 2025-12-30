package com.redmadrobot.konfeature.builder

import com.redmadrobot.konfeature.*
import com.redmadrobot.konfeature.exception.ConfigNameAlreadyExistException
import com.redmadrobot.konfeature.exception.KeyDuplicationException
import com.redmadrobot.konfeature.exception.NoFeatureConfigException
import com.redmadrobot.konfeature.exception.SourceNameAlreadyExistException
import com.redmadrobot.konfeature.source.FeatureSource
import com.redmadrobot.konfeature.source.Interceptor

/**
 * Builder class for configuring and creating a [Konfeature] instance.
 *
 * KonfeatureBuilder uses the builder pattern to configure all aspects of a Konfeature
 * instance including sources, interceptors, feature configurations, and logging.
 * It validates the configuration during the build process to ensure consistency.
 *
 * Example usage:
 * ```kotlin
 * val konfeature = konfeature {
 *     addSource(FirebaseFeatureSource(remoteConfig))
 *     addInterceptor(DebugPanelInterceptor())
 *     register(MyFeatureConfig())
 *     setLogger(TimberLogger())
 * }
 * ```
 *
 * @see konfeature for the DSL function that creates and configures a builder
 */
public class KonfeatureBuilder {
    private val sources = mutableListOf<FeatureSource>()
    private var interceptors = mutableListOf<Interceptor>()
    private var spec = mutableListOf<FeatureConfig>()
    private var logger: Logger? = null

    /**
     * Adds an interceptor to the Konfeature configuration.
     *
     * @param interceptor the interceptor to add
     * @return this builder instance for method chaining
     */
    public fun addInterceptor(interceptor: Interceptor): KonfeatureBuilder {
        interceptors.add(interceptor)
        return this
    }

    /**
     * Adds a feature source to the Konfeature configuration.
     *
     * @param source the source to add
     * @return this builder instance for method chaining
     * @throws SourceNameAlreadyExistException if a source with the same name already exists
     */
    public fun addSource(source: FeatureSource): KonfeatureBuilder {
        if (sources.any { it.name == source.name }) {
            throw SourceNameAlreadyExistException(source.name)
        }

        sources.add(source)
        return this
    }

    /**
     * Registers a feature configuration with the Konfeature instance.
     *
     * @param featureConfig the configuration to register
     * @return this builder instance for method chaining
     * @throws ConfigNameAlreadyExistException if a config with the same name already exists
     */
    public fun register(featureConfig: FeatureConfig): KonfeatureBuilder {
        if (spec.any { it.name == featureConfig.name }) {
            throw ConfigNameAlreadyExistException(featureConfig.name)
        }
        spec.add(featureConfig)
        return this
    }

    /**
     * Sets the logger for the Konfeature instance.
     *
     * @param logger the logger to use for Konfeature events
     * @return this builder instance for method chaining
     */
    public fun setLogger(logger: Logger): KonfeatureBuilder {
        this.logger = logger
        return this
    }

    /**
     * Builds and returns a configured Konfeature instance.
     *
     * This method validates the configuration and throws exceptions if:
     * - No feature configurations are registered
     * - Feature configurations have duplicate keys
     *
     * @return a fully configured Konfeature instance
     * @throws NoFeatureConfigException if no configurations are registered
     * @throws KeyDuplicationException if configurations have duplicate keys
     */
    public fun build(): Konfeature {
        if (spec.isEmpty()) throw NoFeatureConfigException()

        spec.forEach(::validateConfigSpec)

        return KonfeatureImpl(
            sources = sources,
            interceptors = interceptors,
            logger = logger,
            spec = spec
        ).also { toggleEase ->
            spec.forEach { values ->
                values.bind(toggleEase)
            }
        }
    }

    private fun validateConfigSpec(config: FeatureConfigSpec) {
        val counter = mutableMapOf<String, Int>().withDefault { 0 }
        var hasDuplicates = false
        config.values.forEach { valueSpec ->
            val value = counter.getValue(valueSpec.key)
            if (value > 0) {
                hasDuplicates = true
            }
            counter[valueSpec.key] = value + 1
        }

        if (hasDuplicates) {
            val values = counter.asSequence()
                .filter { it.value > 1 }
                .map { it.key }
                .toList()
            throw KeyDuplicationException(values, config.name)
        } else if (counter.isEmpty()) {
            logger?.logWarn("Config '${config.name}' is empty")
        }
    }
}

/**
 * DSL function for creating and configuring a Konfeature instance.
 *
 * This function provides a convenient way to configure a Konfeature instance
 * using a builder DSL. It creates a KonfeatureBuilder, applies the configuration
 * block, and returns the built Konfeature instance.
 *
 * @param build configuration block applied to the KonfeatureBuilder
 * @return a configured Konfeature instance
 */
public fun konfeature(build: KonfeatureBuilder.() -> Unit): Konfeature {
    return KonfeatureBuilder().apply(build).build()
}
