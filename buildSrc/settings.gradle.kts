@file:Suppress("StringLiteralDuplication")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google {
            content {
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
                includeGroupAndSubgroups("androidx")
            }
        }
        mavenCentral()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google {
            content {
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
                includeGroupAndSubgroups("androidx")
            }
        }

        mavenCentral()
        gradlePluginPortal()
    }

    versionCatalogs {
        // Keep it in sync with the root settings.gradle.kts
        val rmrVersion = "2026.06.26"
        create("rmr") {
            from("com.redmadrobot.versions:versions-redmadrobot:$rmrVersion")
        }
        create("androidx") {
            from("com.redmadrobot.versions:versions-androidx:$rmrVersion")
        }
        create("stack") {
            from("com.redmadrobot.versions:versions-stack:$rmrVersion")
        }
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
