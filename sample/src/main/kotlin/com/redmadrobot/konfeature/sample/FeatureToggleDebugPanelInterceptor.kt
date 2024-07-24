package com.redmadrobot.konfeature.sample

import com.redmadrobot.konfeature.source.FeatureValueSource
import com.redmadrobot.konfeature.source.Interceptor

class FeatureToggleDebugPanelInterceptor : Interceptor {

    private val values = mutableMapOf<String, Any>()

    override val name: String = "DebugPanelInterceptor"

    override fun intercept(valueSource: FeatureValueSource, key: String, value: Any): Any? {
        return values[key]
    }

    fun setFeatureValue(key: String, value: Any) {
        values[key] = value
    }
}
