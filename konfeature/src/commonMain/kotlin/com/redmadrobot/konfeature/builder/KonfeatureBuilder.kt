package com.redmadrobot.konfeature.builder

import com.redmadrobot.konfeature.Konfeature
import com.redmadrobot.konfeature.Logger
import com.redmadrobot.konfeature.delegate.FeatureGroup
import com.redmadrobot.konfeature.delegate.FeatureGroupSpec
import com.redmadrobot.konfeature.exception.GroupNameAlreadyExistException
import com.redmadrobot.konfeature.exception.KeyDuplicationException
import com.redmadrobot.konfeature.exception.NoFeatureGroupException
import com.redmadrobot.konfeature.exception.SourceNameAlreadyExistException
import com.redmadrobot.konfeature.logWarn
import com.redmadrobot.konfeature.source.FeatureSource
import com.redmadrobot.konfeature.source.Interceptor

public class KonfeatureBuilder {
    private val sources = mutableListOf<FeatureSource>()
    private var interceptors = mutableListOf<Interceptor>()
    private var featureGroups = mutableListOf<FeatureGroup>()
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

    public fun register(featureGroup: FeatureGroup): KonfeatureBuilder {
        if (featureGroups.any { it.name == featureGroup.name }) {
            throw GroupNameAlreadyExistException(featureGroup.name)
        }
        featureGroups.add(featureGroup)
        return this
    }

    public fun setLogger(logger: Logger): KonfeatureBuilder {
        this.logger = logger
        return this
    }

    public fun build(): Konfeature {
        if (featureGroups.isEmpty()) {
            throw NoFeatureGroupException()
        }

        featureGroups.forEach(::validateGroupSpec)

        return KonfeatureImpl(
            sources = sources,
            interceptors = interceptors,
            logger = logger,
            spec = featureGroups
        ).also { toggleEase ->
            featureGroups.forEach { values ->
                values.bind(toggleEase)
            }
        }
    }

    private fun validateGroupSpec(group: FeatureGroupSpec) {
        val counter = mutableMapOf<String, Int>().withDefault { 0 }
        var hasDuplicates = false
        group.values.forEach { valueSpec ->
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
            throw KeyDuplicationException(values, group.name)
        } else if (counter.isEmpty()) {
            logger?.logWarn("Group '${group.name}' is empty")
        }
    }
}

public fun konfeature(build: KonfeatureBuilder.() -> Unit): Konfeature {
    return KonfeatureBuilder().apply(build).build()
}
