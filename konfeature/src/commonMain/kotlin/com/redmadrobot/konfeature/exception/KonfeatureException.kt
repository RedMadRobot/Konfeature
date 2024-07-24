package com.redmadrobot.konfeature.exception

public sealed class KonfeatureException(messageProvider: () -> String) : Exception(messageProvider.invoke())

public class ConfigNameAlreadyExistException(
    name: String
) : KonfeatureException({ "feature config with name '$name' already registered" })

public class KeyDuplicationException(
    values: List<String>,
    config: String
) : KonfeatureException({
    val duplicatedValues = values.joinToString(separator = ", ", transform = { "'$it'" })
    "values with keys <$duplicatedValues> are duplicated in config '$config'"
})

public class NoFeatureConfigException : KonfeatureException({ "No feature config added" })

public class SourceNameAlreadyExistException(
    name: String
) : KonfeatureException({ "source with name '$name' already registered" })
