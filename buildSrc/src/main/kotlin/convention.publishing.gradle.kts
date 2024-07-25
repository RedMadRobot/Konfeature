import com.redmadrobot.build.dsl.*
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()

    pom {
        name.convention(project.name)
        description.convention(project.description)

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
