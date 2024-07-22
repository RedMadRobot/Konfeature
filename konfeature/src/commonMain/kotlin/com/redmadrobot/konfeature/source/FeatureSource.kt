package com.redmadrobot.konfeature.source

public interface FeatureSource {

    public val name: String

    public fun get(key: String): Any?
}
