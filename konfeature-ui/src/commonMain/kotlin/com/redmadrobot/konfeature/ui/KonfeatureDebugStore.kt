package com.redmadrobot.konfeature.ui

import com.redmadrobot.konfeature.Logger
import com.redmadrobot.konfeature.ui.data.DefaultKonfeatureDebugStore
import kotlinx.coroutines.flow.StateFlow

/**
 * Stores and persists feature value overrides applied by a debug panel.
 *
 * This is an abstraction: [KonfeatureDebugInterceptor], [KonfeatureDebugPanel] and the debug
 * `ViewModel` depend only on this interface, so the backing storage is swappable. The
 * DataStore-backed default is created via [create]; provide a custom implementation for
 * non-persistent overrides or platforms without a file system.
 *
 * Prefer the [create] factory, which constructs the default store **and** completes the initial
 * [load] in one `suspend` call, so the returned store already reflects any persisted overrides —
 * callers cannot forget to load it:
 *
 * ```kotlin
 * // Once on startup, from a suitable coroutine scope (DI init, application scope, etc.):
 * val store = KonfeatureDebugStore.create(
 *     path = context.filesDir.resolve("konfeature_debug.preferences_pb").absolutePath,
 *     logger = konfeatureLogger, // optional
 * )
 * val interceptor = KonfeatureDebugInterceptor(store)
 *
 * val konfeature = konfeature {
 *     addInterceptor(interceptor)
 * }
 * ```
 *
 * **Hold a single instance.** Creation is a one-time startup operation; provide the store as a
 * singleton (DI graph or a shared `object`) and reuse it for the interceptor and the debug screen.
 *
 * The current overrides are exposed both as a [values] flow (observed by the debug screen) and via
 * the synchronous, non-blocking [currentValue].
 */
public interface KonfeatureDebugStore {

    /** Current overrides. Updated by [load] and by the mutate operations. */
    public val values: StateFlow<Map<String, Any>>

    /**
     * Loads overrides from the backing storage. Idempotent: a repeated call re-reads the storage.
     */
    public suspend fun load()

    /**
     * Sets an override for [key].
     *
     * Only persistable types are accepted: `Boolean`, `Int`, `Long`, `Float`, `Double` and `String`.
     * Passing any other type throws [IllegalArgumentException] and leaves the current overrides
     * untouched — this prevents an override that would apply in memory but silently vanish on the next
     * [load]. Guard against it by not offering an editor for [KonfeatureValueType.OTHER] values.
     *
     * @throws IllegalArgumentException if [value] is of a non-persistable type.
     */
    public suspend fun setValue(key: String, value: Any)

    /** Removes the override for a single [key]. */
    public suspend fun resetValue(key: String)

    /** Removes all overrides. */
    public suspend fun resetAll()

    /** Synchronous read of the current override. */
    public fun currentValue(key: String): Any?

    public companion object {

        /**
         * Creates the default, DataStore-backed store for [path] and completes the initial [load]
         * before returning.
         *
         * Call once on startup from any suitable coroutine scope; the returned store already
         * reflects persisted overrides.
         *
         * @param path absolute file path for the DataStore storage file. Platform-specific: on
         *   Android use `context.filesDir`, on iOS use `NSDocumentDirectory`.
         * @param logger optional logger.
         */
        public suspend fun create(path: String, logger: Logger? = null): KonfeatureDebugStore {
            return DefaultKonfeatureDebugStore(path, logger).apply { load() }
        }
    }
}
