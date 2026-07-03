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
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(projects.konfeature)
            implementation(stack.kotlinx.coroutines.core)
        }
    }
}
