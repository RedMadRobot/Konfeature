package com.redmadrobot.konfeature.ui.presentation.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

private const val ENTER_DURATION_MILLIS = 220
private const val EXIT_DURATION_MILLIS = 180

@Composable
internal fun LazyItemScope.AnimatedFilterItem(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = ENTER_DURATION_MILLIS)) +
            expandVertically(animationSpec = tween(durationMillis = ENTER_DURATION_MILLIS)),
        exit = fadeOut(animationSpec = tween(durationMillis = EXIT_DURATION_MILLIS)) +
            shrinkVertically(animationSpec = tween(durationMillis = EXIT_DURATION_MILLIS)),
        modifier = modifier.animateItem(),
    ) {
        content()
    }
}
