plugins {
    kotlin("jvm")
    alias(stack.plugins.kotlin.compose)
    alias(stack.plugins.composeMultiplatform)
    application
    convention.detekt
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(projects.konfeatureUi)
    implementation(compose.desktop.currentOs)
    implementation(stack.compose.material3)
    implementation(stack.kotlinx.coroutines.swing)
}

application {
    mainClass = "com.redmadrobot.konfeature.sample.AppKt"
}
