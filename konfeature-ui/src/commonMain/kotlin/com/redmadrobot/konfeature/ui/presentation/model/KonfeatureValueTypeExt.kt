package com.redmadrobot.konfeature.ui.presentation.model

import com.redmadrobot.konfeature.ui.KonfeatureValueType

/**
 * Maps a resolved value to its [KonfeatureValueType].
 */
internal fun valueTypeOf(value: Any): KonfeatureValueType = when (value) {
    is Boolean -> KonfeatureValueType.BOOLEAN
    is Int -> KonfeatureValueType.INT
    is Long -> KonfeatureValueType.LONG
    is Float -> KonfeatureValueType.FLOAT
    is Double -> KonfeatureValueType.DOUBLE
    is String -> KonfeatureValueType.STRING
    else -> KonfeatureValueType.OTHER
}
