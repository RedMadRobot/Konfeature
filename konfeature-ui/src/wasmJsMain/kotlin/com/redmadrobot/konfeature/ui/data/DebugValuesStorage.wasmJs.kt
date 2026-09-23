@file:OptIn(ExperimentalWasmJsInterop::class)

package com.redmadrobot.konfeature.ui.data

import androidx.datastore.core.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.js.ExperimentalWasmJsInterop

internal actual fun createDebugValuesStorage(path: String): DebugValuesStorage {
    return LocalStorageDebugValuesStorage(key = path)
}

/**
 * Persists the overrides blob to the browser's `localStorage` under [key].
 *
 * DataStore is not an option on web: its `wasmJs` factories are `TODO("Not yet implemented")` in
 * the version this library depends on, and the `WebLocalStorage` storage that replaces them only
 * exists in DataStore 1.3.0-alpha. Since the store persists a single JSON string, one
 * `localStorage` entry is all that is needed.
 *
 * Every access is guarded. A browser can refuse `localStorage` outright (Safari with site data
 * blocked, a sandboxed cross-origin iframe) or reject a write once the quota is exhausted. Those
 * failures surface as [kotlin.js.JsException], which extends `Throwable` and **not** `Exception`,
 * so they would slip past the catch-all in [DefaultKonfeatureDebugStore] and crash the app instead
 * of degrading to "no persisted overrides". They are re-thrown as [IOException] — exactly what an
 * unreadable DataStore file raises — so the store handles both backends the same way.
 */
private class LocalStorageDebugValuesStorage(private val key: String) : DebugValuesStorage {

    override suspend fun read(): String? = guarded("read") { localStorageGetItem(key) }

    override suspend fun write(raw: String?) {
        guarded("write") {
            if (raw == null) localStorageRemoveItem(key) else localStorageSetItem(key, raw)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private inline fun <T> guarded(operation: String, block: () -> T): T {
        return try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            throw IOException("localStorage $operation failed for key '$key': ${e.message}", e)
        }
    }
}

private fun localStorageGetItem(key: String): String? = js("window.localStorage.getItem(key)")

private fun localStorageSetItem(key: String, value: String) {
    js("window.localStorage.setItem(key, value)")
}

private fun localStorageRemoveItem(key: String) {
    js("window.localStorage.removeItem(key)")
}
