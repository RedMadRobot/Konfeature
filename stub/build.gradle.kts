plugins {
    id("com.redmadrobot.kotlin-library")
    convention.publishing
    convention.detekt
}

description = "%stub%"

dependencies {
    api(kotlin("stdlib"))
}
