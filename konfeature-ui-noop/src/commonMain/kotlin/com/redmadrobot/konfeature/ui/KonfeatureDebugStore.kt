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
 *
 * @param path unused; accepted only to match the real constructor signature.
 * @param logger unused; accepted only to match the real constructor signature.
 */
public class KonfeatureDebugStore(
    path: String,
    logger: Logger? = null,
) {

    private val _values = MutableStateFlow<Map<String, Any>>(emptyMap())

    /** Always an empty map: the no-op store holds no overrides. */
    public val values: StateFlow<Map<String, Any>> = _values.asStateFlow()

    /** No-op. */
    public suspend fun load() {
        // no-op
    }

    /** No-op. */
    public suspend fun setValue(key: String, value: Any) {
        // no-op
    }

    /** No-op. */
    public suspend fun resetValue(key: String) {
        // no-op
    }

    /** No-op. */
    public suspend fun resetAll() {
        // no-op
    }

    /** Always returns `null`: the no-op store has no overrides. */
    @Suppress("FunctionOnlyReturningConstant")
    public fun currentValue(key: String): Any? = null

    public companion object {

        /** Returns a no-op store. */
        public suspend fun create(path: String, logger: Logger? = null): KonfeatureDebugStore {
            return KonfeatureDebugStore(path, logger)
        }
    }
}
