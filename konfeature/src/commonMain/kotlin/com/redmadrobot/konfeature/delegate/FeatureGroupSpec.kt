package com.redmadrobot.konfeature.delegate

public interface FeatureGroupSpec {
    public val name: String
    public val description: String
    public val values: List<FeatureValueSpec<out Any>>
}
