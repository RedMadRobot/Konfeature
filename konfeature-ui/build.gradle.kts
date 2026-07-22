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

    iosArm64()
    iosSimulatorArm64()

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
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.redmadrobot.konfeature.ui.resources"
    generateResClass = auto
}
