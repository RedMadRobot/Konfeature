import io.gitlab.arturbosch.detekt.*

plugins {
    id("io.gitlab.arturbosch.detekt")
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = JavaVersion.VERSION_11.toString()
}
tasks.withType<DetektCreateBaselineTask>().configureEach {
    jvmTarget = JavaVersion.VERSION_11.toString()
}

dependencies {
    //noinspection UseTomlInstead
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-rules-libraries:1.23.6")
    //noinspection UseTomlInstead
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.6")
}
