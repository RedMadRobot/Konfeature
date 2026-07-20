plugins {
    kotlin("multiplatform")
    alias(stack.plugins.poko)
    convention.publishing
    convention.detekt
}

description = "Kotlin library for working with feature remote configuration"

kotlin {
    explicitApi()
    jvm()

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(kotlin("stdlib"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(stack.kotest.assertions.core)
        }
    }

    @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
    abiValidation()
}

tasks.check.configure {
    dependsOn("checkLegacyAbi")
}
