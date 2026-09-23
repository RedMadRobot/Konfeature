package com.redmadrobot.konfeature.build

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Verifies konfeature-ui-noop is a faithful drop-in replacement for konfeature-ui: both ABI dumps
 * must declare exactly the same targets, declarations and members. Anything an app can reference
 * against konfeature-ui must also compile against konfeature-ui-noop, so there is no list of
 * exempted declarations — only compiler-generated synthetics are ignored (see [parseDump]).
 *
 * Works on both dump flavours — `.klib.api` and JVM `.api` — as they share the layout this relies on:
 * a declaration starts at column 0 and its members are indented under it.
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
        val noop = parseDump(noopDump.get().asFile)
        val ui = parseDump(uiDump.get().asFile)

        val problems = buildList {
            if (noop.targets != ui.targets) {
                add("Targets differ:\n  konfeature-ui:      ${ui.targets}\n  konfeature-ui-noop: ${noop.targets}")
            }
            (ui.declarations.keys + noop.declarations.keys).sorted().forEach { header ->
                val uiMembers = ui.declarations[header]
                val noopMembers = noop.declarations[header]
                when {
                    noopMembers == null -> add("Missing in konfeature-ui-noop:\n  $header")
                    uiMembers == null -> add("Not in konfeature-ui, but declared in konfeature-ui-noop:\n  $header")
                    uiMembers != noopMembers -> add(
                        buildString {
                            append("Members differ in:\n  ").append(header)
                            (uiMembers - noopMembers).forEach { append("\n    - missing in noop: ").append(it) }
                            (noopMembers - uiMembers).forEach { append("\n    + extra in noop:   ").append(it) }
                        },
                    )
                }
            }
        }

        if (problems.isEmpty()) return

        error(
            "konfeature-ui-noop public API is out of sync with konfeature-ui " +
                "(${uiDump.get().asFile.name} vs ${noopDump.get().asFile.name}).\n" +
                "A declaration whose signature changed shows up as both missing and extra.\n\n" +
                problems.joinToString("\n\n"),
        )
    }

    /**
     * @property targets the dump-level `// Targets: [...]` line of a klib dump, `null` for JVM dumps.
     * @property declarations top-level declaration header -> its member lines, trimmed. Nesting is not
     *   lost by flattening: klib member lines carry their fully qualified signature, and JVM dumps list
     *   nested classes as separate `Outer$Inner` declarations.
     */
    private class Dump(val targets: String?, val declarations: Map<String, Set<String>>)

    private companion object {

        const val TARGETS_PREFIX = "// Targets:"

        /**
         * Parses an ABI dump, dropping what necessarily differs between the two modules and carries no
         * API a caller could use:
         * - the comment header, including the "Library unique name" line (targets are kept);
         * - Compose compiler synthetics: `$stable` fields, top-level `$stableprop` properties and their
         *   getters, and `ComposableSingletons$…Kt` lambda holders. They depend on implementation
         *   details (class stability, lambdas inside a function body), not on the declared API.
         *
         * Target restrictions of a single declaration or member (a `// Targets: [...]` line right above
         * it) are kept attached to it, so a declaration available on fewer targets on one side is
         * reported as a difference.
         */
        fun parseDump(file: File): Dump {
            var targets: String? = null
            val declarations = mutableMapOf<String, MutableSet<String>>()
            var members: MutableSet<String>? = null
            var pendingTargets: String? = null
            var seenDeclaration = false

            for (line in file.readLines()) {
                val trimmed = line.trim()
                val isMember = line.firstOrNull()?.isWhitespace() == true
                when {
                    trimmed.isEmpty() || trimmed == "}" -> Unit

                    // Anything before the first declaration is the dump's comment header.
                    !seenDeclaration && trimmed.startsWith("//") -> {
                        if (targets == null && trimmed.startsWith(TARGETS_PREFIX)) targets = trimmed
                    }

                    trimmed.startsWith(TARGETS_PREFIX) -> pendingTargets = trimmed

                    isMember -> {
                        val member = withTargets(pendingTargets, trimmed)
                        pendingTargets = null
                        if (!trimmed.contains("\$stable")) {
                            checkNotNull(members) { "${file.name}: member outside of a declaration: $line" }
                                .add(member)
                        }
                    }

                    else -> {
                        seenDeclaration = true
                        val header = withTargets(pendingTargets, trimmed)
                        pendingTargets = null
                        members = if (isSynthetic(trimmed)) {
                            mutableSetOf()
                        } else {
                            declarations.getOrPut(header) { mutableSetOf() }
                        }
                    }
                }
            }
            return Dump(targets, declarations)
        }

        fun isSynthetic(header: String): Boolean =
            header.contains("\$stableprop") || header.contains("ComposableSingletons\$")

        fun withTargets(targets: String?, line: String): String = if (targets == null) line else "$targets $line"
    }
}
