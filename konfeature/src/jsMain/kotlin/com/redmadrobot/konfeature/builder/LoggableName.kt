package com.redmadrobot.konfeature.builder

import kotlin.reflect.KClass

internal actual val KClass<*>.loggableName: String?
    get() = simpleName
