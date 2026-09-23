package com.redmadrobot.konfeature.build

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

/** Fails when the freshly generated [actualDump] differs from the committed [referenceDump]. */
abstract class CheckAbiDumpTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val actualDump: RegularFileProperty

    /**
     * Absolute path of the committed dump. Not an [InputFile]: a missing reference must fail with the
     * update hint below, not with a Gradle input validation error.
     */
    @get:Input
    abstract val referenceDumpPath: Property<String>

    /** Task to run to regenerate the reference, named in the failure message. */
    @get:Input
    abstract val updateTaskPath: Property<String>

    @TaskAction
    fun check() {
        val reference = File(referenceDumpPath.get())
        val hint = "Run ${updateTaskPath.get()} and commit the result if the change is intended."
        check(reference.exists()) { "ABI reference dump $reference does not exist. $hint" }

        val expected = reference.readLines()
        val actual = actualDump.get().asFile.readLines()
        if (expected == actual) return

        error(
            buildString {
                append("ABI of ").append(reference.name).append(" differs from the reference dump. ").append(hint)
                (expected - actual.toSet()).forEach { append("\n  - ").append(it) }
                (actual - expected.toSet()).forEach { append("\n  + ").append(it) }
            },
        )
    }
}
