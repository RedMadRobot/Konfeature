package com.redmadrobot.konfeature.helper

import com.redmadrobot.konfeature.delegate.FeatureGroup
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

fun createEmptyFeatureGroup(
    name: String,
    description: String = "test description for $name",
): FeatureGroup {
    return object : FeatureGroup(name = name, description = description) {}
}
