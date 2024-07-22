package com.redmadrobot.konfeature.sample

import com.redmadrobot.konfeature.source.FeatureSource

class RemoteFeatureSource : FeatureSource {

    private val store = mutableMapOf<String, Any>().apply {
        put("feature1", false)
        put("feature3", true)
        put("velocity_value", true)
    }

    override val name: String = "RemoteFeatureToggleSource"

    override fun get(key: String): Any? {
        return store[key]
    }
}

class FirebaseFeatureSource : FeatureSource {

    private val store = mutableMapOf<String, Any>().apply {
        put("feature2", true)
        put("puhFeature", "C")
    }

    override val name: String = "FirebaseFeatureToggleSource"

    override fun get(key: String): Any? {
        return store[key]
    }
}
