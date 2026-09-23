package com.redmadrobot.konfeature.build

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.buildtools.api.ExperimentalBuildToolsApi
import org.jetbrains.kotlin.buildtools.api.KotlinToolchains
import org.jetbrains.kotlin.buildtools.api.abi.AbiValidationToolchain.Companion.abiValidation
import org.jetbrains.kotlin.buildtools.api.abi.dumpJvmAbiToStringOperation

/**
 * Dumps the public ABI of compiled JVM class files in the same `.api` format as KGP's `abiValidation`.
 *
 * KGP's own dump only picks up `KotlinAndroidTarget`s, and the target created by the AGP KMP plugin
 * (`com.android.kotlin.multiplatform.library`) is not one, so without this task the Android surface
 * would go unvalidated. It drives the same Build Tools API operation KGP uses, so the output is
 * byte-for-byte comparable with the other `api/` dumps.
 */
@CacheableTask
abstract class DumpJvmAbiTask : DefaultTask() {

    /** Compiled classes to dump. Only `.class` files are read. */
    @get:Classpath
    abstract val classfiles: ConfigurableFileCollection

    /** Build Tools API implementation (`kotlin-build-tools-impl`) with its runtime dependencies. */
    @get:Classpath
    abstract val buildToolsClasspath: ConfigurableFileCollection

    @get:OutputFile
    abstract val dumpFile: RegularFileProperty

    @OptIn(ExperimentalBuildToolsApi::class)
    @TaskAction
    fun dump() {
        val classes = classfiles.asFileTree.filter { it.name.endsWith(".class") }.map { it.toPath() }
        val toolchains = KotlinToolchains.loadImplementation(buildToolsClasspath.map { it.toPath() })

        val output = dumpFile.get().asFile
        output.parentFile.mkdirs()
        output.bufferedWriter().use { writer ->
            toolchains.createBuildSession().use { session ->
                session.executeOperation(toolchains.abiValidation.dumpJvmAbiToStringOperation(writer, classes))
            }
        }
    }
}
