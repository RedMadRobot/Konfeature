plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    alias(stack.plugins.kotlin.compose)
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
            implementation(stack.kotlinx.coroutines.core)
            implementation(stack.kotlinx.collections.immutable)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(androidx.datastore.preferences.core)
            implementation(stack.kotlinx.serialization.json)
            implementation(libs.compose.resources)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.redmadrobot.konfeature.ui.resources"
    generateResClass = auto
}
