package com.redmadrobot.konfeature.exception

public sealed class KonfeatureException(messageProvider: () -> String) : Exception(messageProvider.invoke())

public class GroupNameAlreadyExistException(
    name: String
) : KonfeatureException({ "feature group with name '$name' already registered" })

public class KeyDuplicationException(
    values: List<String>,
    group: String
) : KonfeatureException({
    val duplicatedValues = values.joinToString(separator = ", ", transform = { "'$it'" })
    "values with keys <$duplicatedValues> are duplicated in group '$group'"
})

public class NoFeatureGroupException : KonfeatureException({ "No feature group added" })

public class SourceNameAlreadyExistException(
    name: String
) : KonfeatureException({ "source with name '$name' already registered" })
