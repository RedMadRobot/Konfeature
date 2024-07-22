package com.redmadrobot.konfeature.source

public interface Interceptor {

    public val name: String

    public fun intercept(valueSource: FeatureValueSource, key: String, value: Any): Any?
}
