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

    // This module has no Compose dependency, so a klib is all that is needed here — unlike
    // konfeature-ui, it needs no executable bundle to load a Skiko runtime.
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    // The target set is deliberately identical to konfeature-ui's, which is capped at arm64 for
    // Apple by Compose Multiplatform. A drop-in replacement must publish for the same targets, so
    // this module follows suit even though it could support more.
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

val verificationGroup = "verification"

val checkKlibApiMatchesUi = tasks.register<CheckNoopApiTask>("checkKlibApiMatchesUi") {
    group = verificationGroup
    description = "Verifies konfeature-ui-noop klib API stays in sync with konfeature-ui"

    format.set(CheckNoopApiTask.DumpFormat.KLIB)
    noopDump.set(layout.projectDirectory.file("api/konfeature-ui-noop.klib.api"))
    uiDump.set(project(":konfeature-ui").layout.projectDirectory.file("api/konfeature-ui.klib.api"))
}

// The JVM (desktop) target is part of the swappable contract too, and it is dumped separately from
// the klib targets — without this the whole JVM surface would go unverified.
val checkJvmApiMatchesUi = tasks.register<CheckNoopApiTask>("checkJvmApiMatchesUi") {
    group = verificationGroup
    description = "Verifies konfeature-ui-noop JVM API stays in sync with konfeature-ui"

    format.set(CheckNoopApiTask.DumpFormat.JVM)
    noopDump.set(layout.projectDirectory.file("api/jvm/konfeature-ui-noop.api"))
    uiDump.set(project(":konfeature-ui").layout.projectDirectory.file("api/jvm/konfeature-ui.api"))
}

val checkApiMatchesUi = tasks.register("checkApiMatchesUi") {
    group = verificationGroup
    description = "Verifies konfeature-ui-noop public API stays in sync with konfeature-ui"

    dependsOn(checkKlibApiMatchesUi, checkJvmApiMatchesUi)
}

tasks.check.configure {
    dependsOn(checkApiMatchesUi)
}
