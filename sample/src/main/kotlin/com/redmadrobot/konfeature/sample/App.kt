package com.redmadrobot.konfeature.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.redmadrobot.konfeature.Konfeature
import com.redmadrobot.konfeature.Logger
import com.redmadrobot.konfeature.builder.konfeature
import com.redmadrobot.konfeature.ui.KonfeatureDebugInterceptor
import com.redmadrobot.konfeature.ui.KonfeatureDebugPanel
import com.redmadrobot.konfeature.ui.KonfeatureDebugStore
import com.redmadrobot.konfeature.ui.KonfeatureValueInfo
import com.redmadrobot.konfeature.ui.theme.KonfeatureTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

fun main() {
    val logger = object : Logger {
        override fun log(severity: Logger.Severity, message: String) {
            if (severity == Logger.Severity.WARNING) println("${severity.name}: $message")
        }
    }

    val storeFile = File(System.getProperty("user.home"), ".konfeature-sample/debug_overrides.preferences_pb")
    storeFile.parentFile.mkdirs()
    val store = runBlocking { KonfeatureDebugStore.create(path = storeFile.absolutePath, logger = logger) }

    val konfeature = konfeature {
        addSource(RemoteConfigSource())
        addSource(AbTestingSource())
        register(AppearanceConfig())
        register(CheckoutConfig())
        addInterceptor(KonfeatureDebugInterceptor(store))
        setLogger(logger)
    }

    printSpec(konfeature)
    println("Debug overrides are stored in ${storeFile.absolutePath}")

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Konfeature sample",
            state = rememberWindowState(size = DpSize(width = 520.dp, height = 800.dp)),
        ) {
            SampleScreen(konfeature = konfeature, store = store)
        }
    }
}

@Composable
private fun SampleScreen(konfeature: Konfeature, store: KonfeatureDebugStore) {
    val isSystemDark = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(isSystemDark) }
    var editedValue by remember { mutableStateOf<KonfeatureValueInfo?>(null) }
    val scope = rememberCoroutineScope()

    // Without an explicit KonfeatureTheme the panel just follows the system theme.
    KonfeatureTheme(isDarkTheme = isDarkTheme) {
        Column(modifier = Modifier.background(color = KonfeatureTheme.colors.background)) {
            ThemeSwitch(isDarkTheme = isDarkTheme, onChange = { isDarkTheme = it })
            KonfeatureDebugPanel(
                konfeature = konfeature,
                store = store,
                onValueClick = { editedValue = it },
            )
        }

        editedValue?.let { info ->
            EditValueDialog(
                info = info,
                onSave = { newValue -> scope.launch { store.setValue(info.key, newValue) } },
                onReset = { scope.launch { store.resetValue(info.key) } },
                onDismiss = { editedValue = null },
            )
        }
    }
}

@Composable
private fun ThemeSwitch(isDarkTheme: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (isDarkTheme) "Night" else "Day",
            color = KonfeatureTheme.colors.contentPrimary,
            modifier = Modifier.weight(weight = 1f),
        )
        Switch(checked = isDarkTheme, onCheckedChange = onChange)
    }
}

private fun printSpec(konfeature: Konfeature) {
    konfeature.spec.forEach { config ->
        println("Config '${config.name}' (${config.description})")
        config.values.forEach { spec ->
            val value = konfeature.getValue(spec)
            println("  ${spec.key} = ${value.value}  <- ${value.source}")
        }
    }
}
