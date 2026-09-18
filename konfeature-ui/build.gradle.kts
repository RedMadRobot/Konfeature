import com.redmadrobot.konfeature.Versions

plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    alias(stack.plugins.kotlin.compose)
    alias(stack.plugins.composeMultiplatform)
    alias(stack.plugins.poko)
    convention.publishing
    convention.detekt
}

description = "Compose Multiplatform UI for managing Konfeature"

kotlin {
    explicitApi()
    jvmToolchain(17)

    android {
        namespace = "com.redmadrobot.konfeature.ui"
        compileSdk = Versions.COMPILE_SDK
        minSdk = Versions.MIN_SDK
        androidResources {
            enable = true
        }
    }

    // Desktop (JVM) target of Compose Multiplatform.
    jvm()

    // Compose Multiplatform for web (Kotlin/Wasm) is in Beta.
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        // Compose UI tests on wasmJs need an executable bundle to load the Skiko runtime.
        binaries.executable()
    }

    // Compose Multiplatform 1.12 publishes Apple targets only for arm64:
    // there are no iosX64/macosX64 artifacts of compose-ui to depend on.
    iosArm64()
    iosSimulatorArm64()

    macosArm64()

    // Overrides are persisted through DataStore everywhere it has a real implementation; the web
    // target uses localStorage instead, see DebugValuesStorage.
    applyDefaultHierarchyTemplate {
        common {
            group("dataStore") {
                withJvm()
                withAndroidTarget()
                withApple()
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.konfeature)
            api(stack.compose.runtime)
            api(stack.compose.ui)
            implementation(stack.kotlinx.coroutines.core)
            implementation(stack.kotlinx.collections.immutable)
            implementation(stack.compose.foundation)
            implementation(stack.compose.material3)
            implementation(stack.lifecycle.viewmodel.compose)
            implementation(androidx.datastore.preferences.core)
            implementation(stack.kotlinx.serialization.json)
            implementation(stack.compose.resources)
        }
        wasmJsTest.dependencies {
            implementation(kotlin("test"))
            implementation(stack.kotlinx.coroutines.test)
        }

        jvmMain.dependencies {
            // The debug panel's ViewModel works on Dispatchers.Main, which on desktop is provided
            // by the Swing dispatcher module. Without it the panel crashes on first interaction.
            implementation(stack.kotlinx.coroutines.swing)
        }
    }

    @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
    abiValidation()
}

tasks.check.configure {
    dependsOn("checkLegacyAbi")
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.redmadrobot.konfeature.ui.resources"
    generateResClass = auto
}
