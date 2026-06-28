plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.composeMultiplatform)
    convention.publishing
    convention.detekt
}

description = "Compose Multiplatform UI for managing Konfeature"

kotlin {
    explicitApi()
    jvmToolchain(17)

    android {
        namespace = "com.redmadrobot.konfeature.ui"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources {
            enable = true
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(projects.konfeature)
            api(libs.compose.runtime)
            api(libs.compose.ui)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.datastore.preferences.core)
            implementation(libs.serialization.json)
            implementation(libs.compose.resources)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.redmadrobot.konfeature.ui.resources"
    generateResClass = auto
}
