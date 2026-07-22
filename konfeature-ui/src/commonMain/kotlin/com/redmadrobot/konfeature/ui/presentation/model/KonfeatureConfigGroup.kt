package com.redmadrobot.konfeature.ui.presentation.model

import kotlinx.collections.immutable.ImmutableList

internal data class KonfeatureConfigGroup(
    val config: KonfeatureItem.Config,
    val values: ImmutableList<KonfeatureItem.Value>,
)
