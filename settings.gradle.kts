@file:Suppress("StringLiteralDuplication")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            content {
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
                includeGroupAndSubgroups("androidx")
            }
        }
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        google {
            content {
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
                includeGroupAndSubgroups("androidx")
            }
        }
        mavenCentral()
    }

    versionCatalogs {
        // Keep it in sync with buildSrc/settings.gradle.kts
        val rmrVersion = "2026.09.07"
        create("rmr") {
            from("com.redmadrobot.versions:versions-redmadrobot:$rmrVersion")
        }
        create("androidx") {
            from("com.redmadrobot.versions:versions-androidx:$rmrVersion")
        }
        create("stack") {
            from("com.redmadrobot.versions:versions-stack:$rmrVersion")
        }
    }
}

rootProject.name = "konfeature-root"

include(
    ":sample",
    ":konfeature",
    ":konfeature-ui",
    ":konfeature-ui-noop",
)
