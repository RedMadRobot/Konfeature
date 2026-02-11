plugins {
    kotlin("multiplatform")
    alias(libs.plugins.poko)
    convention.publishing
    convention.detekt
}

description = "Kotlin library for working with feature remote configuration"

kotlin {
    explicitApi()
    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(kotlin("stdlib"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotest.assertions.core)
        }
    }
}
