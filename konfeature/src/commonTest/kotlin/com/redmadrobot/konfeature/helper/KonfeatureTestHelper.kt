package com.redmadrobot.konfeature.helper

import com.redmadrobot.konfeature.FeatureConfig
import com.redmadrobot.konfeature.source.FeatureSource

fun createTestSource(
    name: String,
    values: Map<String, Any> = emptyMap(),
): FeatureSource {
    return object : FeatureSource {

        override val name: String = name

        override fun get(key: String): Any? = values[key]
    }
}

fun createEmptyFeatureConfig(
    name: String,
    description: String = "test description for $name",
): FeatureConfig {
    return object : FeatureConfig(name = name, description = description) {}
}
