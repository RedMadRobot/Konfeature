package com.redmadrobot.konfeature.ui.data

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
 */
private class LocalStorageDebugValuesStorage(private val key: String) : DebugValuesStorage {

    override suspend fun read(): String? = localStorageGetItem(key)

    override suspend fun write(raw: String?) {
        if (raw == null) localStorageRemoveItem(key) else localStorageSetItem(key, raw)
    }
}

private fun localStorageGetItem(key: String): String? = js("window.localStorage.getItem(key)")

private fun localStorageSetItem(key: String, value: String) {
    js("window.localStorage.setItem(key, value)")
}

private fun localStorageRemoveItem(key: String) {
    js("window.localStorage.removeItem(key)")
}
