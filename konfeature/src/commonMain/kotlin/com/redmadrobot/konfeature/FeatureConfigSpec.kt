package com.redmadrobot.konfeature

public interface FeatureConfigSpec {
    public val name: String
    public val description: String
    public val values: List<FeatureValueSpec<out Any>>
}
