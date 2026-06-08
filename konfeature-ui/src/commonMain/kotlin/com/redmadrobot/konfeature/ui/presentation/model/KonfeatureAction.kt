package com.redmadrobot.konfeature.ui.presentation.model

internal sealed interface KonfeatureAction {
    data object RefreshClick : KonfeatureAction
    data object CollapseAllClick : KonfeatureAction
    data object ResetAllClick : KonfeatureAction

    data class ConfigHeaderClick(val configName: String) : KonfeatureAction

    data class SearchQueryChange(val query: String) : KonfeatureAction

    data class ToggleChange(val key: String, val checked: Boolean) : KonfeatureAction
    data class ValueClick(val key: String) : KonfeatureAction
}
