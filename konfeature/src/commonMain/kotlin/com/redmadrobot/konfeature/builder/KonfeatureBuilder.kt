package com.redmadrobot.konfeature.builder

import com.redmadrobot.konfeature.Konfeature
import com.redmadrobot.konfeature.Logger
import com.redmadrobot.konfeature.FeatureConfig
import com.redmadrobot.konfeature.FeatureConfigSpec
import com.redmadrobot.konfeature.exception.ConfigNameAlreadyExistException
import com.redmadrobot.konfeature.exception.KeyDuplicationException
import com.redmadrobot.konfeature.exception.NoFeatureConfigException
import com.redmadrobot.konfeature.exception.SourceNameAlreadyExistException
import com.redmadrobot.konfeature.logWarn
import com.redmadrobot.konfeature.source.FeatureSource
import com.redmadrobot.konfeature.source.Interceptor

public class KonfeatureBuilder {
    private val sources = mutableListOf<FeatureSource>()
    private var interceptors = mutableListOf<Interceptor>()
    private var spec = mutableListOf<FeatureConfig>()
    private var logger: Logger? = null

    public fun addInterceptor(interceptor: Interceptor): KonfeatureBuilder {
        interceptors.add(interceptor)
        return this
    }

    public fun addSource(source: FeatureSource): KonfeatureBuilder {
        if (sources.any { it.name == source.name }) {
            throw SourceNameAlreadyExistException(source.name)
        }

        sources.add(source)
        return this
    }

    public fun register(featureConfig: FeatureConfig): KonfeatureBuilder {
        if (spec.any { it.name == featureConfig.name }) {
            throw ConfigNameAlreadyExistException(featureConfig.name)
        }
        spec.add(featureConfig)
        return this
    }

    public fun setLogger(logger: Logger): KonfeatureBuilder {
        this.logger = logger
        return this
    }

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

public fun konfeature(build: KonfeatureBuilder.() -> Unit): Konfeature {
    return KonfeatureBuilder().apply(build).build()
}
