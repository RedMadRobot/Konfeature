package com.redmadrobot.konfeature.builder

import kotlin.reflect.KClass

/**
 * Name of the class as it is shown in log messages.
 *
 * It is the fully qualified name everywhere except Kotlin/JS, which has no
 * [KClass.qualifiedName] at all and falls back to [KClass.simpleName].
 */
internal expect val KClass<*>.loggableName: String?
