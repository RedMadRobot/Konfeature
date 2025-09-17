package com.redmadrobot.konfeature.source

/**
 * Strategy for selecting which sources can provide values for a configuration element.
 *
 * Source selection strategies allow fine-grained control over which data sources
 * are consulted for specific configuration values. This enables scenarios like:
 * - Development-only values that should ignore remote sources
 * - Production values that should only come from verified sources
 * - Feature flags that should only use specific A/B testing sources
 *
 * The strategy receives the names of all available sources and returns the subset
 * that should be consulted for a particular configuration element.
 */
public fun interface SourceSelectionStrategy {

    /**
     * Selects which sources should be consulted for a configuration value.
     *
     * @param names the names of all available registered sources
     * @return the subset of source names that should be used for this configuration element
     */
    public fun select(names: Set<String>): Set<String>

    public companion object {
        /**
         * Strategy that prohibits using any sources, always falling back to default values.
         *
         * This is the default strategy, ensuring that configuration elements are
         * explicitly opted into remote configuration to prevent accidental dependencies.
         */
        public val None: SourceSelectionStrategy = SourceSelectionStrategy { emptySet() }

        /**
         * Strategy that allows using any available source.
         *
         * This strategy permits the configuration element to use values from any
         * registered source, following the normal resolution order.
         */
        public val Any: SourceSelectionStrategy = SourceSelectionStrategy { it }

        /**
         * Creates a strategy that only allows specific named sources.
         *
         * @param sources the names of sources that should be allowed
         * @return a strategy that only permits the specified sources
         */
        public fun anyOf(vararg sources: String): SourceSelectionStrategy = SourceSelectionStrategy { sources.toSet() }
    }
}
