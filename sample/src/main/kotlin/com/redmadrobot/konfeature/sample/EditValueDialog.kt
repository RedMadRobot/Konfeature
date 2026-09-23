package com.redmadrobot.konfeature.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.redmadrobot.konfeature.ui.KonfeatureValueInfo
import com.redmadrobot.konfeature.ui.KonfeatureValueType

/**
 * Editor for non-boolean values: [com.redmadrobot.konfeature.ui.KonfeatureDebugPanel] toggles booleans
 * itself and hands everything else to the integrator via `onValueClick`.
 */
@Composable
fun EditValueDialog(
    info: KonfeatureValueInfo,
    onSave: (Any) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember(info.key) { mutableStateOf(info.currentValue.toString()) }
    val parsed = parse(text, info.type)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = info.key) },
        text = {
            Column {
                Text(text = info.description)
                Spacer(modifier = Modifier.height(height = 8.dp))
                Text(text = "Type: ${info.type}, default: ${info.defaultValue}, source: ${info.sourceName}")
                Spacer(modifier = Modifier.height(height = 12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    isError = parsed == null,
                    singleLine = true,
                    supportingText = { if (parsed == null) Text(text = "Not a valid ${info.type}") },
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = parsed != null,
                onClick = {
                    parsed?.let(onSave)
                    onDismiss()
                },
            ) { Text(text = "Save") }
        },
        dismissButton = {
            Row {
                if (info.isOverridden) {
                    TextButton(
                        onClick = {
                            onReset()
                            onDismiss()
                        },
                    ) { Text(text = "Reset") }
                }
                TextButton(onClick = onDismiss) { Text(text = "Cancel") }
            }
        },
    )
}

private fun parse(text: String, type: KonfeatureValueType): Any? = when (type) {
    KonfeatureValueType.INT -> text.toIntOrNull()
    KonfeatureValueType.LONG -> text.toLongOrNull()
    KonfeatureValueType.FLOAT -> text.toFloatOrNull()
    KonfeatureValueType.DOUBLE -> text.toDoubleOrNull()
    KonfeatureValueType.STRING -> text
    KonfeatureValueType.BOOLEAN -> text.toBooleanStrictOrNull()
    KonfeatureValueType.OTHER -> null
}
