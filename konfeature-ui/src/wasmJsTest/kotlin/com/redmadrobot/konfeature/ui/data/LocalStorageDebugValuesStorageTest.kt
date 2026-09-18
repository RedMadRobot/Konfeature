@file:OptIn(ExperimentalWasmJsInterop::class)

package com.redmadrobot.konfeature.ui.data

import androidx.datastore.core.IOException
import kotlinx.coroutines.test.runTest
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * Covers the browser backend of [DebugValuesStorage], the one platform where overrides are not
 * persisted through DataStore.
 */
class LocalStorageDebugValuesStorageTest {

    private val storage = createDebugValuesStorage(KEY)

    @AfterTest
    fun tearDown() = runTest {
        restoreLocalStorage()
        storage.write(null)
    }

    @Test
    fun readsBackWhatItWrote() = runTest {
        storage.write(BLOB)

        assertEquals(BLOB, storage.read())
    }

    @Test
    fun readsNullWhenNothingWasStored() = runTest {
        storage.write(null)

        assertNull(storage.read())
    }

    @Test
    fun overwritesThePreviousBlob() = runTest {
        storage.write(BLOB)
        storage.write("""{"feature2":true}""")

        assertEquals("""{"feature2":true}""", storage.read())
    }

    /**
     * A browser can refuse `localStorage` (site data blocked, sandboxed cross-origin iframe) or
     * reject a write over quota. Those arrive as `JsException`, which extends `Throwable` and not
     * `Exception`, so unless they are translated here they slip past the catch-all in
     * [DefaultKonfeatureDebugStore] and take the whole app down with them.
     */
    @Test
    fun translatesBrowserFailuresIntoIOException() = runTest {
        breakLocalStorageReads()

        assertFailsWith<IOException> { storage.read() }
    }

    @Test
    fun aRefusingBrowserLeavesTheStoreLoadableWithoutOverrides() = runTest {
        breakLocalStorageReads()
        val store = DefaultKonfeatureDebugStore(KEY)

        store.load()

        assertEquals(emptyMap(), store.values.value)
    }

    private companion object {
        private const val KEY = "konfeature_debug_storage_test"
        private const val BLOB = """{"feature1":false,"velocity_value":120}"""
    }
}

/** Shadows `Storage.prototype.getItem` with a throwing own property, as a hostile browser would. */
private fun breakLocalStorageReads() {
    js("window.localStorage.getItem = function () { throw new Error('SecurityError: access denied') }")
}

/** Drops the own property again so the prototype implementation takes over. */
private fun restoreLocalStorage() {
    js("delete window.localStorage.getItem")
}
