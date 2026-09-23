plugins {
    kotlin("multiplatform")
    alias(stack.plugins.poko)
    convention.publishing
    convention.detekt
}

description = "Kotlin library for working with feature remote configuration"

kotlin {
    explicitApi()
    jvmToolchain(17)

    // A deliberate subset: the core has no third-party dependencies and could support every Kotlin
    // target, but only the platforms Konfeature is actually used on are built and published.
    jvm()

    js {
        browser()
        nodejs()
    }

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }

    iosArm64()
    iosSimulatorArm64()

    macosArm64()

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
