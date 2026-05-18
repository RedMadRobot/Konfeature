package com.redmadrobot.konfeature.ui.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.redmadrobot.konfeature.ui.presentation.theme.ContentColors
import com.redmadrobot.konfeature.ui.presentation.theme.KonfeatureTypography
import com.redmadrobot.konfeature.ui.presentation.theme.StrokeColors

private val defaultTextStyle: TextStyle
    @Composable get() = KonfeatureTypography.bodyMedium.copy(
        color = ContentColors.primary,
    )

private val defaultColors: TextFieldColors
    @Composable get() = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ContentColors.accent,
        unfocusedBorderColor = StrokeColors.secondary,
        focusedLabelColor = ContentColors.accent,
        unfocusedLabelColor = ContentColors.tertiary,
        errorBorderColor = ContentColors.error,
        cursorColor = ContentColors.accent,
    )

@Composable
public fun PanelStyledTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    textStyle: TextStyle = defaultTextStyle,
    colors: TextFieldColors = defaultColors
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = label, style = KonfeatureTypography.bodyMedium) },
            isError = isError,
            singleLine = true,
            keyboardOptions = keyboardOptions,
            textStyle = textStyle,
            colors = colors,
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = KonfeatureTypography.bodySmall,
                color = ContentColors.error,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
