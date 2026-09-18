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

/**
 * Verifies konfeature-ui-noop stays a faithful drop-in replacement for konfeature-ui: every
 * public declaration of konfeature-ui must be mirrored member-for-member in konfeature-ui-noop,
 * except the UI-only surface (Compose panel, theme, resources, and `$stableprop` synthetics),
 * which the no-op module intentionally omits.
 *
 * ABI dumps are compared as sets of top-level declaration blocks, so new public API in
 * konfeature-ui fails the build by default until it is either mirrored in konfeature-ui-noop or
 * added to the UI-only lists below.
 *
 * Both dump flavours are supported through [format], because the two modules publish for JVM
 * targets as well as for klib ones and a swap has to hold on every target: `.klib.api` dumps name
 * declarations as `some.package/Name`, `.api` (JVM) dumps as `some/package/Name`.
 */
abstract class CheckNoopApiTask : DefaultTask() {

    /** Flavour of the two dumps being compared. Both must be of the same flavour. */
    enum class DumpFormat { KLIB, JVM }

    @get:Input
    abstract val format: Property<DumpFormat>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val noopDump: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val uiDump: RegularFileProperty

    @TaskAction
    fun check() {
        val dumpFormat = format.get()
        val noopBlocks = blocks(noopDump.get().asFile)
        val uiBlocks = blocks(uiDump.get().asFile)

        val missingInNoop = uiBlocks.filterNot { isUiOnly(it, dumpFormat) } - noopBlocks
        val extraInNoop = noopBlocks - uiBlocks

        if (missingInNoop.isEmpty() && extraInNoop.isEmpty()) return

        error(
            buildString {
                appendLine("konfeature-ui-noop public API is out of sync with konfeature-ui ($dumpFormat dumps).")
                if (missingInNoop.isNotEmpty()) {
                    appendLine()
                    appendLine("Present in konfeature-ui but missing/different in konfeature-ui-noop")
                    appendLine("(implement it in konfeature-ui-noop, or add it to the UI-only lists if it is not part of the contract):")
                    appendLine()
                    append(missingInNoop.joinToString("\n\n"))
                    appendLine()
                }
                if (extraInNoop.isNotEmpty()) {
                    appendLine()
                    appendLine("Present in konfeature-ui-noop but missing/different in konfeature-ui")
                    appendLine("(a member was added/changed/removed on the konfeature-ui-noop side):")
                    appendLine()
                    append(extraInNoop.joinToString("\n\n"))
                }
            },
        )
    }

    private fun isUiOnly(block: String, format: DumpFormat): Boolean {
        val header = block.lineSequence().first()
        if (header.contains("\$stableprop")) return true
        val (packageName, simpleNames) = when (format) {
            DumpFormat.KLIB -> parseKlibName(header) ?: return false
            DumpFormat.JVM -> parseJvmName(header) ?: return false
        }
        if (UI_ONLY_PACKAGES.any { packageName == it || packageName.startsWith("$it.") }) return true
        return simpleNames.any { "$packageName/$it" in UI_ONLY_DECLARATIONS }
    }

    private companion object {

        val KLIB_NAME_REGEX = Regex("""[\w.]+/\w+""")
        val JVM_NAME_REGEX = Regex("""[\w/$]+/[\w$]+""")

        // Declarations in konfeature-ui that are intentionally UI-only and NOT part of the
        // no-op contract. Everything else in konfeature-ui must be mirrored in konfeature-ui-noop.
        val UI_ONLY_PACKAGES = listOf(
            "com.redmadrobot.konfeature.ui.presentation",
            "com.redmadrobot.konfeature.ui.resources",
        )
        val UI_ONLY_DECLARATIONS = setOf(
            "com.redmadrobot.konfeature.ui/KonfeatureValueType",
            "com.redmadrobot.konfeature.ui/KonfeatureValueInfo",
            "com.redmadrobot.konfeature.ui/KonfeatureDebugPanel",
        )

        /** `final class com.redmadrobot.konfeature.ui/KonfeatureDebugPanel` -> package + name. */
        fun parseKlibName(header: String): Pair<String, List<String>>? {
            val name = KLIB_NAME_REGEX.find(header)?.value ?: return null
            return name.substringBefore('/') to listOf(name.substringAfter('/'))
        }

        /**
         * `public final class com/redmadrobot/konfeature/ui/KonfeatureDebugPanelKt {` -> package
         * plus every simple name the declaration could have been generated from.
         *
         * The JVM backend derives extra top-level classes from a single source declaration: the
         * `FooKt` file facade of `Foo.kt` and the `ComposableSingletons$FooKt` lambda holder. They
         * carry no API of their own, so they are UI-only exactly when `Foo` is.
         */
        fun parseJvmName(header: String): Pair<String, List<String>>? {
            val name = JVM_NAME_REGEX.find(header)?.value ?: return null
            val packageName = name.substringBeforeLast('/').replace('/', '.')
            val simpleName = name.substringAfterLast('/')
            val candidates = linkedSetOf(simpleName)
            candidates += simpleName.substringAfterLast('$')
            candidates.toList().forEach { candidate ->
                if (candidate.endsWith("Kt")) candidates += candidate.removeSuffix("Kt")
            }
            return packageName to candidates.toList()
        }

        /**
         * Split an ABI dump into top-level declaration blocks (a declaration plus its
         * indented members), dropping the comment header — including the "Library unique name"
         * line, which necessarily differs between the two modules.
         *
         * Member lines holding a Compose stability synthetic (`$stable`) are dropped as well: the
         * Compose compiler plugin runs only over konfeature-ui, so those fields appear on one side
         * only while carrying no API a caller could use. Top-level `$stableprop` declarations are
         * left alone here and excluded by [isUiOnly], so their own members stay attached to them.
         */
        fun blocks(file: File): Set<String> =
            file.readLines()
                .filterNot { line ->
                    line.startsWith("//") ||
                        line.isBlank() ||
                        (line.first().isWhitespace() && line.contains("\$stable"))
                }
                .fold(mutableListOf<StringBuilder>()) { acc, line ->
                    if (!line.first().isWhitespace()) {
                        acc.add(StringBuilder(line))
                    } else {
                        acc.last().append('\n').append(line)
                    }
                    acc
                }
                .map(StringBuilder::toString)
                .toSet()
    }
}
