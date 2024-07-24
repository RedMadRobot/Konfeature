package com.redmadrobot.konfeature.sample

import com.redmadrobot.konfeature.Logger
import com.redmadrobot.konfeature.builder.konfeature

fun main() {
    val featureConfig = SampleFeatureConfig()

    val debugPanelInterceptor = FeatureToggleDebugPanelInterceptor()

    val logger = object : Logger {
        override fun log(severity: Logger.Severity, message: String) {
            println("${severity.name}: $message")
        }
    }

    val konfeature = konfeature {
        addSource(RemoteFeatureSource())
        addSource(FirebaseFeatureSource())
        register(featureConfig)
        addInterceptor(debugPanelInterceptor)
        setLogger(logger)
    }

    konfeature.spec.forEach {
        println("Spec: --name: '${it.name}', description: '${it.description}'")
        it.values.forEach(::println)
    }

    println()
    val spec = konfeature.spec.first().values.first()
    println("getFeatureValue('${spec.key}') -> ${konfeature.getValue(spec)}")

    println()
    println("feature1: " + featureConfig.isFeature1Enabled)
    println("feature2: " + featureConfig.isFeature2Enabled)
    println("feature3: " + featureConfig.isFeature3Enabled)
    println("velocity: " + featureConfig.velocity)
    println("puhFeature: " + featureConfig.puhFeature)

    debugPanelInterceptor.setFeatureValue("feature2", false)
    println()
    println("debugPanelInterceptor.setFeatureValue(\"feature2\", false)")
    println("feature1: " + featureConfig.isFeature1Enabled)
    println("feature2: " + featureConfig.isFeature2Enabled)
    println("feature3: " + featureConfig.isFeature3Enabled)
}
