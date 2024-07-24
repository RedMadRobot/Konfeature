package com.redmadrobot.konfeature

import com.redmadrobot.konfeature.source.SourceSelectionStrategy
import dev.drewhamilton.poko.Poko

@Poko
public class FeatureValueSpec<T : Any>(
    public val key: String,
    public val description: String,
    public val defaultValue: T,
    public val sourceSelectionStrategy: SourceSelectionStrategy
)
