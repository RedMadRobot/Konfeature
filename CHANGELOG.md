## [Unreleased]

### Changes

- New optional **`konfeature-ui`** module — a Compose Multiplatform debug panel (Android, iOS):
  - `KonfeatureDebugPanel` — screen listing all registered configs (grouped, collapsible, searchable by key/description/config name) with the current value and its source, allowing runtime overrides. Booleans are toggled inline; other values report a `KonfeatureValueInfo` via `onValueClick` for a custom editor. Shows a per-config override count and supports resetting a single override or all of them.
  - `KonfeatureDebugInterceptor` — `Interceptor` that applies the overrides made in the panel
  - `KonfeatureDebugStore` — persists overrides to disk via DataStore and exposes them as a `StateFlow`; `setValue` rejects non-persistable types (only `Boolean`, `Int`, `Long`, `Float`, `Double` and `String` are supported)
  - Customizable theme — wrap the panel in `KonfeatureTheme` with a `KonfeatureColors` palette (built via `lightKonfeatureColors` / `darkKonfeatureColors`) to brand it; falls back to a system-driven light/dark default
- New **`konfeature-ui-noop`** module — an API-compatible no-op replacement for `konfeature-ui`, to strip the debug tooling from release builds
- Updated Kotlin to 2.4.0 and refreshed dependencies (kotlinx-coroutines, kotlinx-serialization, kotlinx-collections-immutable, AndroidX Lifecycle, DataStore)
- Build tooling: adopted the RedMadRobot version catalogs (`rmr`, `androidx`, `stack`), updated Gradle to 9.4.1 and AGP to 9.2.1
- Dropped the `iosX64` (Intel iOS simulator) target from `konfeature`; the remaining Apple targets are `iosArm64` and `iosSimulatorArm64`

## [1.0.0] (2026-02-25)

### Changed

- Added support for iOS targets for use in Kotlin Multiplatform projects
- Updated versions of libraries and plugins
- Updated version of AGP to 9.0.0
- Added binary compatibility validation

## v0.1.0 (2024-07-25)

Initial public release

[unreleased]: https://github.com/RedMadRobot/Konfeature/compare/v1.0.0...main
[1.0.0]: https://github.com/RedMadRobot/Konfeature/compare/v0.1.0...v1.0.0
