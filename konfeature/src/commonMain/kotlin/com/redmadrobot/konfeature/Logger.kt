package com.redmadrobot.konfeature

/**
 * Interface for logging Konfeature events and errors.
 *
 * The logger is used to track configuration value access, type mismatches,
 * and other diagnostic information. Implementations can integrate with
 * existing logging frameworks like Timber, SLF4J, or custom solutions.
 *
 * Events that are logged include:
 * - Configuration value access with source information
 * - Type mismatch warnings when sources return unexpected types
 * - Configuration validation warnings
 */
public interface Logger {

    /**
     * Logs a message with the specified severity level.
     *
     * @param severity the severity level of the log message
     * @param message the message to log
     */
    public fun log(severity: Severity, message: String)

    /**
     * Severity levels for log messages.
     */
    public enum class Severity {
        /**
         * Warning level for non-critical issues like type mismatches or empty configurations.
         */
        WARNING,

        /**
         * Information level for normal operations like value access and source information.
         */
        INFO
    }
}

internal fun Logger.logWarn(message: String) {
    log(Logger.Severity.WARNING, message)
}

internal fun Logger.logInfo(message: String) {
    log(Logger.Severity.INFO, message)
}
