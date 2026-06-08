package com.redmadrobot.konfeature.ui.presentation.model

internal sealed class KonfeatureValue {

    data class Bool(val value: Boolean) : KonfeatureValue()

    data class Int64(val value: Long) : KonfeatureValue()

    data class Float64(val value: Double) : KonfeatureValue()

    data class Text(val value: String) : KonfeatureValue()

    data class Unsupported(val raw: Any) : KonfeatureValue()

    fun unwrap(): Any = when (this) {
        is Bool -> value
        is Int64 -> value
        is Float64 -> value
        is Text -> value
        is Unsupported -> raw
    }

    companion object {
        fun of(raw: Any): KonfeatureValue = when (raw) {
            is Boolean -> Bool(raw)
            is Long -> Int64(raw)
            is Int -> Int64(raw.toLong())
            is Double -> Float64(raw)
            is Float -> Float64(raw.toDouble())
            is String -> Text(raw)
            else -> Unsupported(raw)
        }
    }
}
