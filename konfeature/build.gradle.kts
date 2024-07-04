plugins {
    id("com.redmadrobot.kotlin-library")
    convention.publishing
    convention.detekt
}

description = "konfeature"

dependencies {
    api(kotlin("stdlib"))
}
