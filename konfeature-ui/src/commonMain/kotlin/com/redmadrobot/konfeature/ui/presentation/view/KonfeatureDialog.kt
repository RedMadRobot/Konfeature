package com.redmadrobot.konfeature.ui.presentation.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.redmadrobot.konfeature.ui.presentation.theme.ContentColors
import com.redmadrobot.konfeature.ui.presentation.theme.KonfeatureShapes
import com.redmadrobot.konfeature.ui.presentation.theme.KonfeatureTypography
import com.redmadrobot.konfeature.ui.presentation.theme.SurfaceColors

@Composable
internal fun KonfeatureDialog(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = KonfeatureShapes.dialog,
            color = SurfaceColors.dialog,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        ) {
            Column(modifier = Modifier.padding(all = 24.dp)) {
                Text(
                    text = title,
                    style = KonfeatureTypography.titleLarge,
                    color = ContentColors.primary,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                content()
            }
        }
    }
}
