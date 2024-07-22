package com.redmadrobot.konfeature.source

import dev.drewhamilton.poko.Poko

public sealed class FeatureValueSource {

    @Poko
    public class Source(public val name: String) : FeatureValueSource()

    @Poko
    public class Interceptor(public val name: String) : FeatureValueSource()

    @Suppress("ConvertObjectToDataObject")
    public object Default : FeatureValueSource() {
        override fun toString(): String = "Default"
    }
}
