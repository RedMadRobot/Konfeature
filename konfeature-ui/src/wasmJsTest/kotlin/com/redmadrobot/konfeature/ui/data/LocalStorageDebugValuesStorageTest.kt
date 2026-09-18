package com.redmadrobot.konfeature.ui.data

import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Covers the browser backend of [DebugValuesStorage], the one platform where overrides are not
 * persisted through DataStore.
 */
class LocalStorageDebugValuesStorageTest {

    private val storage = createDebugValuesStorage(KEY)

    @AfterTest
    fun tearDown() = runTest { storage.write(null) }

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

    private companion object {
        private const val KEY = "konfeature_debug_storage_test"
        private const val BLOB = """{"feature1":false,"velocity_value":120}"""
    }
}
