package com.redmadrobot.konfeature.ui.presentation.data

import androidx.compose.ui.graphics.Color

internal sealed interface KonfeatureItem {
    val itemKey: String

    data class Config(
        val name: String,
        val description: String,
    ) : KonfeatureItem {
        override val itemKey: String
            get() = "config_$name"
    }

    data class Value(
        val key: String,
        val configName: String,
        val value: Any,
        val description: String,
        val sourceName: String,
        val sourceColor: Color,
        val isDebugSource: Boolean
    ) : KonfeatureItem {
        override val itemKey: String
            get() = "value_$key"

        val editAvailable: Boolean
            get() = when (value) {
                is Boolean,
                is String,
                is Long,
                is Double -> true
                else -> false
            }
    }
}
