package com.redmadrobot.konfeature.ui.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.redmadrobot.konfeature.Logger
import com.redmadrobot.konfeature.ui.KonfeatureDebugStore
import com.redmadrobot.konfeature.ui.info
import com.redmadrobot.konfeature.ui.warn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import okio.Path.Companion.toPath

/**
 * Default [com.redmadrobot.konfeature.ui.KonfeatureDebugStore] that persists overrides to a DataStore preferences file.
 *
 * Construct it through [com.redmadrobot.konfeature.ui.KonfeatureDebugStore.Companion.create], which also completes the initial [load].
 *
 * @param path absolute file path for the DataStore storage file.
 * @param logger optional logger.
 */
internal class DefaultKonfeatureDebugStore(
    path: String,
    private val logger: Logger? = null,
) : KonfeatureDebugStore {

    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
        produceFile = { path.toPath() },
    )

    private val _values = MutableStateFlow<Map<String, Any>>(emptyMap())

    override val values: StateFlow<Map<String, Any>> = _values.asStateFlow()

    @Suppress("TooGenericExceptionCaught")
    override suspend fun load() {
        try {
            val prefs = dataStore.data.first()
            val json = prefs[VALUES_KEY]
            if (json != null) {
                _values.value = deserializeMap(json)
            }
        } catch (e: CorruptionException) {
            // The storage file exists but can't be de-serialized (data-format corruption).
            logger?.warn("Debug overrides storage is corrupted, starting empty: ${e.message}")
        } catch (e: IOException) {
            // Transient disk read failure (missing file, permissions, etc.).
            logger?.warn("Failed to read debug overrides from disk, starting empty: ${e.message}")
        } catch (e: SerializationException) {
            // The persisted blob is present but not valid JSON of the expected shape.
            logger?.warn("Failed to parse persisted debug overrides, starting empty: ${e.message}")
        } catch (e: Exception) {
            // Catch-all so a debug tool can always run without persisted overrides.
            logger?.warn("Failed to load debug overrides, starting empty: ${e.message}")
        }
    }

    override suspend fun setValue(key: String, value: Any) {
        require(isPersistable(value)) {
            "Cannot override '$key': type '${value::class.simpleName}' is not persistable. " +
                "Only Boolean, Int, Long, Float, Double and String are supported."
        }
        _values.update { it + (key to value) }
        persist()
        logger?.info("Set debug override '$key' = '$value'")
    }

    override suspend fun resetValue(key: String) {
        _values.update { it - key }
        persist()
        logger?.info("Reset debug override '$key'")
    }

    override suspend fun resetAll() {
        _values.update { emptyMap() }
        persist()
        logger?.info("Reset all debug overrides")
    }

    override fun currentValue(key: String): Any? = _values.value[key]

    @Suppress("TooGenericExceptionCaught")
    private suspend fun persist() {
        val map = _values.value
        try {
            dataStore.edit { prefs ->
                if (map.isEmpty()) {
                    prefs.remove(VALUES_KEY)
                } else {
                    prefs[VALUES_KEY] = serializeMap(map, logger)
                }
            }
        } catch (e: CorruptionException) {
            // Existing storage can't be read back to be rewritten (data-format corruption).
            logger?.warn("Cannot persist debug overrides, storage is corrupted: ${e.message}")
        } catch (e: IOException) {
            // Most likely cause here: out of disk space or missing write permissions.
            logger?.warn("Failed to write debug overrides to disk: ${e.message}")
        } catch (e: Exception) {
            // The in-memory override is already applied; a debug tool can tolerate a failed disk
            // write. Surface the failure but don't crash the coroutine scope that launched the
            // mutation — the override just won't survive the next reload.
            logger?.warn("Failed to persist debug overrides: ${e.message}")
        }
    }

    private companion object {

        private val VALUES_KEY = stringPreferencesKey("debug_values")

        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = false
        }

        /**
         * The set of types the store can persist. Kept as a single source of truth for [setValue]'s
         * guard and [serializeMap]'s dispatch, so an override can never take effect in memory only to
         * silently vanish on the next [load]. Both lists must stay in sync.
         */
        private fun isPersistable(value: Any): Boolean = when (value) {
            is Boolean, is Int, is Long, is Float, is Double, is String -> true
            else -> false
        }

        private fun serializeMap(map: Map<String, Any>, logger: Logger?): String {
            return buildJsonObject {
                map.forEach { (key, value) ->
                    when (value) {
                        is Boolean -> put(key, value)
                        is Int -> put(key, value)
                        is Long -> put(key, value)
                        is Float -> put(key, value.toDouble())
                        is Double -> put(key, value)
                        is String -> put(key, value)
                        // Unreachable: setValue rejects non-persistable types before they reach _values.
                        // Kept defensively so a future mutation path cannot corrupt the persisted blob.
                        else -> logger?.warn(
                            "Skipping override '$key': unsupported type '${value::class.simpleName}'",
                        )
                    }
                }
            }.toString()
        }

        private fun deserializeMap(raw: String): Map<String, Any> {
            return json.parseToJsonElement(raw).jsonObject.mapNotNull(::parseEntry).toMap()
        }

        private fun parseEntry(entry: Map.Entry<String, JsonElement>): Pair<String, Any>? {
            val primitive = entry.value as? JsonPrimitive
            val value: Any? = when {
                primitive == null -> null
                primitive.isString -> primitive.content
                else -> primitive.booleanOrNull ?: primitive.longOrNull ?: primitive.doubleOrNull
            }
            return value?.let { entry.key to it }
        }
    }
}
