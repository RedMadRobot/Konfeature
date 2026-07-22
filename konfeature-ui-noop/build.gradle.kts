import com.redmadrobot.konfeature.Versions

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

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(projects.konfeature)
            implementation(stack.kotlinx.coroutines.core)
        }
    }
}
