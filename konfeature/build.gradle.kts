plugins {
    kotlin("multiplatform")
    alias(libs.plugins.poko)
    convention.publishing
    convention.detekt
}

kotlin {
    explicitApi()
    jvm()

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
