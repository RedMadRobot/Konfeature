package com.redmadrobot.konfeature.ui

import com.redmadrobot.konfeature.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * No-op counterpart of the `konfeature-ui` [KonfeatureDebugStore], for non-debug builds.
 *
 * It exposes the same public API but holds no overrides and performs no I/O: [values] is always an
 * empty map, [currentValue] always returns `null`, and [load] / [setValue] / [resetValue] /
 * [resetAll] do nothing.
 */
public interface KonfeatureDebugStore {

    /** Always an empty map: the no-op store holds no overrides. */
    public val values: StateFlow<Map<String, Any>>

    /** No-op. */
    public suspend fun load()

    /** No-op. */
    public suspend fun setValue(key: String, value: Any)

    /** No-op. */
    public suspend fun resetValue(key: String)

    /** No-op. */
    public suspend fun resetAll()

    /** Always returns `null`: the no-op store has no overrides. */
    public fun currentValue(key: String): Any?

    public companion object {

        /** Returns a no-op store. The [path] and [logger] are accepted only to match the real API. */
        @Suppress("UNUSED_PARAMETER")
        public suspend fun create(path: String, logger: Logger? = null): KonfeatureDebugStore {
            return NoOpKonfeatureDebugStore()
        }
    }
}

private class NoOpKonfeatureDebugStore : KonfeatureDebugStore {

    private val _values = MutableStateFlow<Map<String, Any>>(emptyMap())

    override val values: StateFlow<Map<String, Any>> = _values.asStateFlow()

    override suspend fun load() {
        // no-op
    }

    override suspend fun setValue(key: String, value: Any) {
        // no-op
    }

    override suspend fun resetValue(key: String) {
        // no-op
    }

    override suspend fun resetAll() {
        // no-op
    }

    @Suppress("FunctionOnlyReturningConstant")
    override fun currentValue(key: String): Any? = null
}
