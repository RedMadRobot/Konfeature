package com.redmadrobot.konfeature

import com.redmadrobot.konfeature.source.FeatureValueSource
import dev.drewhamilton.poko.Poko

@Poko
public class FeatureValue<T>(
    public val source: FeatureValueSource,
    public val value: T,
)
