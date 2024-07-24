package com.redmadrobot.konfeature

public interface Konfeature {

    public val spec: List<FeatureConfigSpec>

    public fun <T : Any> getValue(spec: FeatureValueSpec<T>): FeatureValue<T>
}
