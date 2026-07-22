import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}

java {
    targetCompatibility = JavaVersion.VERSION_17
    sourceCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(rmr.infrastructure.publish)
    implementation(rmr.infrastructure.android)
    implementation(stack.publish.gradlePlugin)
    implementation(stack.gradle.android.cacheFixGradlePlugin)
    implementation(stack.kotlin.gradlePlugin)
    implementation(stack.detekt.gradlePlugin)
    implementation(stack.android.tools.build.gradle)
}
