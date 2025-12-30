package com.redmadrobot.konfeature.exception

/**
 * Base sealed class for all Konfeature-specific exceptions.
 *
 * All exceptions thrown by the Konfeature library inherit from this class,
 * allowing for comprehensive error handling when working with the library.
 */
public sealed class KonfeatureException(messageProvider: () -> String) : Exception(messageProvider.invoke())

/**
 * Exception thrown when attempting to register a feature configuration with a name that already exists.
 *
 * @param name the duplicate configuration name
 */
public class ConfigNameAlreadyExistException(
    name: String
) : KonfeatureException({ "feature config with name '$name' already registered" })

/**
 * Exception thrown when a feature configuration contains duplicate keys.
 *
 * @param values the list of duplicate keys
 * @param config the name of the configuration containing duplicates
 */
public class KeyDuplicationException(
    values: List<String>,
    config: String
) : KonfeatureException({
    val duplicatedValues = values.joinToString(separator = ", ", transform = { "'$it'" })
    "values with keys <$duplicatedValues> are duplicated in config '$config'"
})

/**
 * Exception thrown when attempting to build a Konfeature instance without any registered configurations.
 */
public class NoFeatureConfigException : KonfeatureException({ "No feature config added" })

/**
 * Exception thrown when attempting to register a source with a name that already exists.
 *
 * @param name the duplicate source name
 */
public class SourceNameAlreadyExistException(
    name: String
) : KonfeatureException({ "source with name '$name' already registered" })
