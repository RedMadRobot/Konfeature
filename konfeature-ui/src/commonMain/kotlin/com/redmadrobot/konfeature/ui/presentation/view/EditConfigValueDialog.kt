package com.redmadrobot.konfeature.ui.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.redmadrobot.konfeature.ui.presentation.data.EditDialogState
import com.redmadrobot.konfeature.ui.presentation.data.KonfeatureValue
import com.redmadrobot.konfeature.ui.presentation.theme.ContentColors
import com.redmadrobot.konfeature.ui.presentation.theme.KonfeatureShapes
import com.redmadrobot.konfeature.ui.presentation.theme.KonfeatureTypography
import com.redmadrobot.konfeature.ui.resources.Res
import com.redmadrobot.konfeature.ui.resources.konfeature_plugin_close
import com.redmadrobot.konfeature.ui.resources.konfeature_plugin_edit_dialog_hint_boolean
import com.redmadrobot.konfeature.ui.resources.konfeature_plugin_edit_dialog_hint_double
import com.redmadrobot.konfeature.ui.resources.konfeature_plugin_edit_dialog_hint_long
import com.redmadrobot.konfeature.ui.resources.konfeature_plugin_edit_dialog_hint_string
import com.redmadrobot.konfeature.ui.resources.konfeature_plugin_reset
import com.redmadrobot.konfeature.ui.resources.konfeature_plugin_save
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun EditConfigValueDialog(
    state: EditDialogState,
    onValueChange: (key: String, value: KonfeatureValue) -> Unit,
    onValueReset: (key: String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val initialValue = state.value
    var value by remember { mutableStateOf(initialValue) }
    var isInputEmpty by remember { mutableStateOf(false) }
    val saveEnabled by remember {
        derivedStateOf { !isInputEmpty && initialValue != value }
    }

    KonfeatureDialog(
        title = state.key,
        onDismiss = onDismissRequest,
        modifier = modifier
    ) {
        when (initialValue) {
            is KonfeatureValue.Bool -> BooleanEditInput(
                value = initialValue.value,
                onValueChange = { value = KonfeatureValue.Bool(it) },
            )

            is KonfeatureValue.Int64 -> LongEditInput(
                value = initialValue.value,
                onValueChange = { value = KonfeatureValue.Int64(it) },
                onEmptyInput = { isInputEmpty = it },
            )

            is KonfeatureValue.Float64 -> DoubleEditInput(
                value = initialValue.value,
                onValueChange = { value = KonfeatureValue.Float64(it) },
                onEmptyInput = { isInputEmpty = it },
            )

            is KonfeatureValue.Text -> StringEditInput(
                value = initialValue.value,
                onValueChange = { value = KonfeatureValue.Text(it) },
            )

            is KonfeatureValue.Unsupported -> Unit
        }
        Spacer(modifier = Modifier.height(20.dp))
        EditConfigValueButtons(
            state = state,
            saveEnabled = saveEnabled,
            value = value,
            onValueChange = onValueChange,
            onValueReset = onValueReset,
            onDismissRequest = onDismissRequest,
        )
    }
}

@Composable
@Suppress("LongParameterList")
private fun EditConfigValueButtons(
    state: EditDialogState,
    saveEnabled: Boolean,
    value: KonfeatureValue,
    onValueChange: (key: String, value: KonfeatureValue) -> Unit,
    onValueReset: (key: String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CloseButton(onDismissRequest = onDismissRequest)
        Spacer(modifier = Modifier.weight(weight = 1f))
        if (state.isDebugSource) {
            DebugSourceButton(
                onClick = {
                    onValueReset(state.key)
                    onDismissRequest()
                }
            )
        }
        SaveButton(
            saveEnabled = saveEnabled,
            onClick = {
                onValueChange(state.key, value)
                onDismissRequest()
            }
        )
    }
}

@Composable
private fun SaveButton(
    saveEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        enabled = saveEnabled,
        shape = KonfeatureShapes.medium,
    ) {
        Text(
            text = stringResource(Res.string.konfeature_plugin_save),
            style = KonfeatureTypography.labelLarge,
        )
    }
}

@Composable
private fun DebugSourceButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        modifier = modifier,
        onClick = onClick,
        shape = KonfeatureShapes.medium,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = ContentColors.error,
        ),
    ) {
        Text(
            text = stringResource(Res.string.konfeature_plugin_reset),
            style = KonfeatureTypography.labelLarge,
        )
    }
}

@Composable
private fun CloseButton(onDismissRequest: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        modifier = modifier,
        onClick = onDismissRequest,
        shape = KonfeatureShapes.medium,
    ) {
        Text(
            text = stringResource(Res.string.konfeature_plugin_close),
            style = KonfeatureTypography.labelLarge,
        )
    }
}

@Composable
private fun BooleanEditInput(
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var checked by remember { mutableStateOf(value) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.konfeature_plugin_edit_dialog_hint_boolean),
            style = KonfeatureTypography.bodyMedium,
            color = ContentColors.primary,
            modifier = Modifier.weight(weight = 1f),
        )
        KonfeatureToggle(
            checked = checked,
            onCheckedChange = { newChecked ->
                checked = newChecked
                onValueChange(newChecked)
            },
        )
    }
}

@Composable
private fun LongEditInput(
    value: Long,
    onValueChange: (Long) -> Unit,
    onEmptyInput: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf(value.toString()) }

    KonfeatureTextField(
        value = text,
        onValueChange = { newText ->
            val newValue = newText.toLongOrNull()
            if (newValue != null || newText.isEmpty()) {
                text = newText
                newValue?.let(onValueChange)
                onEmptyInput(newText.isEmpty())
            }
        },
        label = stringResource(Res.string.konfeature_plugin_edit_dialog_hint_long),
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@Composable
private fun DoubleEditInput(
    value: Double,
    onValueChange: (Double) -> Unit,
    onEmptyInput: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text: String by remember { mutableStateOf(value.toString()) }

    KonfeatureTextField(
        value = text,
        onValueChange = { newText ->
            val newValue = newText.toDoubleOrNull()
            if (newValue != null || newText.isEmpty()) {
                text = newText
                newValue?.let(onValueChange)
                onEmptyInput(newText.isEmpty())
            }
        },
        label = stringResource(Res.string.konfeature_plugin_edit_dialog_hint_double),
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    )
}

@Composable
private fun StringEditInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf(value) }

    KonfeatureTextField(
        value = text,
        onValueChange = { newText ->
            text = newText
            onValueChange(newText)
        },
        label = stringResource(Res.string.konfeature_plugin_edit_dialog_hint_string),
        modifier = modifier,
    )
}
