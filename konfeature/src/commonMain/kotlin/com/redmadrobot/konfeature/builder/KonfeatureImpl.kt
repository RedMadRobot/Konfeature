@file:Suppress("NoWildcardImports", "WildcardImport")

package com.redmadrobot.konfeature.builder

import com.redmadrobot.konfeature.*
import com.redmadrobot.konfeature.FeatureConfigSpec
import com.redmadrobot.konfeature.FeatureValueSpec
import com.redmadrobot.konfeature.source.FeatureSource
import com.redmadrobot.konfeature.source.FeatureValueSource
import com.redmadrobot.konfeature.source.Interceptor
import kotlin.reflect.KClass

internal class KonfeatureImpl(
    private val sources: List<FeatureSource>,
    private val interceptors: List<Interceptor>,
    private val logger: Logger?,
    override val spec: List<FeatureConfigSpec>,
) : Konfeature {

    private val sourcesNames = sources.map { it.name }.toSet()

    @Suppress("LoopWithTooManyJumpStatements")
    override fun <T : Any> getValue(spec: FeatureValueSpec<T>): FeatureValue<T> {
        val selectedSourcesNames = spec.sourceSelectionStrategy.select(sourcesNames)

        val expectedClass = spec.defaultValue::class
        var value: T = spec.defaultValue
        var valueSource: FeatureValueSource = FeatureValueSource.Default

        for (source in sources) {
            if (source.name !in selectedSourcesNames) continue
            val actualSourceValue = source.get(spec.key)
            val sourceValue = expectedClass.tryCastOrNull(actualSourceValue)

            if (actualSourceValue != null && sourceValue == null) {
                logger?.logUnexpectedValueType(
                    key = spec.key,
                    source = FeatureValueSource.Source(source.name),
                    value = actualSourceValue,
                    actualClass = actualSourceValue::class.qualifiedName,
                    expectedClass = expectedClass.qualifiedName,
                )
            }

            if (sourceValue != null) {
                value = sourceValue
                valueSource = FeatureValueSource.Source(source.name)
                break
            }
        }

        for (interceptor in interceptors) {
            val actualInterceptorValue = interceptor.intercept(valueSource, spec.key, value)
            val interceptorValue = expectedClass.tryCastOrNull(actualInterceptorValue)

            if (actualInterceptorValue != null && interceptorValue == null) {
                logger?.logUnexpectedValueType(
                    key = spec.key,
                    source = FeatureValueSource.Interceptor(interceptor.name),
                    value = actualInterceptorValue,
                    actualClass = actualInterceptorValue::class.qualifiedName,
                    expectedClass = expectedClass.qualifiedName,
                )
            }

            if (interceptorValue != null) {
                value = interceptorValue
                valueSource = FeatureValueSource.Interceptor(interceptor.name)
            }
        }

        logger?.logInfo("Get value '$value' by key '${spec.key}' from '$valueSource'")

        return FeatureValue(valueSource, value)
    }

    private fun Logger.logUnexpectedValueType(
        key: String,
        source: FeatureValueSource,
        value: Any,
        actualClass: String?,
        expectedClass: String?,
    ) {
        logWarn(
            "Unexpected value type for '$key': " +
                "expected type is '$expectedClass', but " +
                "value from '$source' " +
                "is '$value' " +
                "with type '$actualClass'",
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> KClass<T>.tryCastOrNull(value: Any?): T? {
        return if (isInstance(value)) value as T else null
    }
}
