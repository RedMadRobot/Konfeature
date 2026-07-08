import com.redmadrobot.build.dsl.*

plugins {
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    if (isRunningOnCi) signAllPublications()

    pom {
        name.convention(project.provider { project.name })
        description.convention(project.provider { project.description })

        licenses {
            mit()
        }

        developers {
            developer(id = "AleksandrTabolin", name = "Aleksandr Tabolin", email = "a.tabolin@redmadrobot.com")
        }

        setGitHubProject("RedMadRobot/Konfeature")
    }
}

publishing {
    repositories {
        if (isRunningOnCi) githubPackages("RedMadRobot/Konfeature")
    }
}
