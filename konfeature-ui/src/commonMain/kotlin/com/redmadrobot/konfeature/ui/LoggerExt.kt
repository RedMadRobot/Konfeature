package com.redmadrobot.konfeature.ui

import com.redmadrobot.konfeature.Logger

internal fun Logger.warn(message: String) {
    this.log(Logger.Severity.WARNING, message)
}

internal fun Logger.info(message: String) {
    this.log(Logger.Severity.INFO, message)
}
