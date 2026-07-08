package com.redmadrobot.konfeature.build

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
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
 * Both klib ABI dumps are compared as sets of top-level declaration blocks, so new public API
 * in konfeature-ui fails the build by default until it is either mirrored in konfeature-ui-noop
 * or added to the UI-only lists below.
 */
abstract class CheckNoopApiTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val noopDump: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val uiDump: RegularFileProperty

    @TaskAction
    fun check() {
        val noopBlocks = blocks(noopDump.get().asFile)
        val uiBlocks = blocks(uiDump.get().asFile)

        val missingInNoop = uiBlocks.filterNot { isUiOnly(it) } - noopBlocks
        val extraInNoop = noopBlocks - uiBlocks

        if (missingInNoop.isEmpty() && extraInNoop.isEmpty()) return

        error(
            buildString {
                appendLine("konfeature-ui-noop public API is out of sync with konfeature-ui.")
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

    private fun isUiOnly(block: String): Boolean {
        val header = block.lineSequence().first()
        if (header.contains("\$stableprop")) return true
        val fullyQualifiedName = FULLY_QUALIFIED_NAME_REGEX.find(header)?.value ?: return false
        val packageName = fullyQualifiedName.substringBefore('/')
        return fullyQualifiedName in UI_ONLY_DECLARATIONS ||
            UI_ONLY_PACKAGES.any { packageName == it || packageName.startsWith("$it.") }
    }

    private companion object {
        val FULLY_QUALIFIED_NAME_REGEX = Regex("""[\w.]+/\w+""")

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

        /**
         * Split a klib ABI dump into top-level declaration blocks (a declaration plus its
         * indented members), dropping the comment header — including the "Library unique name"
         * line, which necessarily differs between the two modules.
         */
        fun blocks(file: File): Set<String> =
            file.readLines()
                .filterNot { it.startsWith("//") || it.isBlank() }
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
