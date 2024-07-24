package com.redmadrobot.konfeature

public interface Logger {

    public fun log(severity: Severity, message: String)

    public enum class Severity {
        WARNING, INFO
    }
}

internal fun Logger.logWarn(message: String) {
    log(Logger.Severity.WARNING, message)
}

internal fun Logger.logInfo(message: String) {
    log(Logger.Severity.INFO, message)
}
