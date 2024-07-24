package com.redmadrobot.konfeature.source

public fun interface SourceSelectionStrategy {

    public fun select(names: Set<String>): Set<String>

    public companion object {
        public val None: SourceSelectionStrategy = SourceSelectionStrategy { emptySet() }
        public val Any: SourceSelectionStrategy = SourceSelectionStrategy { it }

        public fun anyOf(vararg sources: String): SourceSelectionStrategy = SourceSelectionStrategy { sources.toSet() }
    }
}
