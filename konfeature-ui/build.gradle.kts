plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
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
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(projects.konfeature)
            implementation(libs.compose.runtime)
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
    publicResClass = false
    packageOfResClass = "com.redmadrobot.konfeature.ui.resources"
}
