package com.redmadrobot.konfeature.ui.data

/**
 * Raw persistence behind [DefaultKonfeatureDebugStore].
 *
 * The store keeps all overrides in a single JSON string, so a backend only has to persist one blob.
 * That is what makes the web target possible: [createDebugValuesStorage] is backed by DataStore
 * everywhere it exists and by `localStorage` in the browser.
 */
internal interface DebugValuesStorage {

    /** Reads the persisted blob, or `null` if nothing was stored yet. */
    suspend fun read(): String?

    /** Persists [raw], or erases the stored blob when it is `null`. */
    suspend fun write(raw: String?)
}

/** Creates the platform storage for [path] — a file path on disk, a storage key on web. */
internal expect fun createDebugValuesStorage(path: String): DebugValuesStorage
