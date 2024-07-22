package com.redmadrobot.konfeature

import com.redmadrobot.konfeature.delegate.FeatureGroupSpec
import com.redmadrobot.konfeature.delegate.FeatureValueSpec

public interface Konfeature {

    public val spec: List<FeatureGroupSpec>

    public fun <T : Any> getValue(spec: FeatureValueSpec<T>): FeatureValue<T>
}
