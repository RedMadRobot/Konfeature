import com.redmadrobot.konfeature.Versions
import com.redmadrobot.konfeature.build.CheckNoopApiTask

plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    convention.publishing
    convention.detekt
}

description = "No-op replacement for konfeature-ui, for non-debug builds"

kotlin {
    explicitApi()
    jvmToolchain(17)

    android {
        namespace = "com.redmadrobot.konfeature.ui.noop"
        compileSdk = Versions.COMPILE_SDK
        minSdk = Versions.MIN_SDK
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

    sourceSets {
        commonMain.dependencies {
            api(projects.konfeature)
            implementation(stack.kotlinx.coroutines.core)
        }
    }

    @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
    abiValidation()
}

tasks.check.configure {
    dependsOn("checkLegacyAbi")
}

val checkApiMatchesUi by tasks.registering(CheckNoopApiTask::class) {
    group = "verification"
    description = "Verifies konfeature-ui-noop public API stays in sync with konfeature-ui"

    noopDump.set(layout.projectDirectory.file("api/konfeature-ui-noop.klib.api"))
    uiDump.set(project(":konfeature-ui").layout.projectDirectory.file("api/konfeature-ui.klib.api"))
}

tasks.check.configure {
    dependsOn(checkApiMatchesUi)
}
