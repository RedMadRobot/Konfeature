import com.redmadrobot.konfeature.Versions
import com.redmadrobot.konfeature.build.CheckNoopApiTask

plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    // The no-op panel and theme are @Composable: without the Compose compiler their ABI would not
    // match konfeature-ui's (no Composer parameters), and the modules would not be swappable.
    alias(stack.plugins.kotlin.compose)
    alias(stack.plugins.poko)
    convention.publishing
    convention.abi.android
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

    // Unlike konfeature-ui, this module has no Compose UI tests, so it needs no executable bundle
    // to load a Skiko runtime.
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
            // Same api surface as konfeature-ui: Modifier, Color and Composable leak into signatures.
            api(stack.compose.runtime)
            api(stack.compose.ui)
            // isSystemInDarkTheme(), the KonfeatureTheme default, as in konfeature-ui.
            implementation(stack.compose.foundation)
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
val uiProject = project(":konfeature-ui")

val checkKlibApiMatchesUi = tasks.register<CheckNoopApiTask>("checkKlibApiMatchesUi") {
    group = verificationGroup
    description = "Verifies konfeature-ui-noop klib API stays in sync with konfeature-ui"

    noopDump.set(layout.projectDirectory.file("api/konfeature-ui-noop.klib.api"))
    uiDump.set(uiProject.layout.projectDirectory.file("api/konfeature-ui.klib.api"))
}

// The JVM (desktop) target is part of the swappable contract too, and it is dumped separately from
// the klib targets — without this the whole JVM surface would go unverified.
val checkJvmApiMatchesUi = tasks.register<CheckNoopApiTask>("checkJvmApiMatchesUi") {
    group = verificationGroup
    description = "Verifies konfeature-ui-noop JVM API stays in sync with konfeature-ui"

    noopDump.set(layout.projectDirectory.file("api/jvm/konfeature-ui-noop.api"))
    uiDump.set(uiProject.layout.projectDirectory.file("api/jvm/konfeature-ui.api"))
}

// Android is dumped by convention.abi.android, since KGP's abiValidation skips the AGP KMP target.
val checkAndroidApiMatchesUi = tasks.register<CheckNoopApiTask>("checkAndroidApiMatchesUi") {
    group = verificationGroup
    description = "Verifies konfeature-ui-noop Android API stays in sync with konfeature-ui"

    noopDump.set(layout.projectDirectory.file("api/android/konfeature-ui-noop.api"))
    uiDump.set(uiProject.layout.projectDirectory.file("api/android/konfeature-ui.api"))
}

val checkApiMatchesUi = tasks.register("checkApiMatchesUi") {
    group = verificationGroup
    description = "Verifies konfeature-ui-noop public API stays in sync with konfeature-ui"

    dependsOn(checkKlibApiMatchesUi, checkJvmApiMatchesUi, checkAndroidApiMatchesUi)
}

tasks.check.configure {
    dependsOn(checkApiMatchesUi)
}
