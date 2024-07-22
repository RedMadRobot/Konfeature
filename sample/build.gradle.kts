plugins {
    kotlin("jvm")
    application
    convention.detekt
}
dependencies {
    implementation(projects.konfeature)
}

application {
    mainClass = "com.redmadrobot.konfeature.sample.AppKt"
}
