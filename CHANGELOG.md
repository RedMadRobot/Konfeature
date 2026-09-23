## [Unreleased]

### Changes

- *No changes*

## [1.2.0] (2026-09-23)

### Changes

- **Breaking:** the theme API of `konfeature-ui` (`KonfeatureTheme`, `KonfeatureColors`, `lightKonfeatureColors`, `darkKonfeatureColors`) moved from `com.redmadrobot.konfeature.ui.presentation.theme` to `com.redmadrobot.konfeature.ui.theme`
- **Breaking:** the generated Compose resources class `com.redmadrobot.konfeature.ui.resources.Res` of `konfeature-ui` is no longer public
- **Breaking:** Android consumers of `konfeature-ui` / `konfeature-ui-noop` must compile against API 37 (`compileSdk = 37`), as required by Compose Multiplatform 1.12
- `konfeature-ui-noop` now mirrors the whole public API of `konfeature-ui`, including a no-op `KonfeatureDebugPanel`, `KonfeatureValueInfo` / `KonfeatureValueType` and the theme, so the panel can be referenced from code shared with release builds. The module now depends on Compose
- `konfeature` gains the `js`, `wasmJs` and `macosArm64` targets, in addition to `jvm`, `iosArm64` and `iosSimulatorArm64`
- `konfeature-ui` and `konfeature-ui-noop` gain the JVM (Compose Desktop), `macosArm64` and `wasmJs` targets. On `wasmJs`, `KonfeatureDebugStore.create` persists overrides to `localStorage` and treats `path` as the storage key; if the browser refuses storage, the store starts empty instead of crashing
- Updated Kotlin to 2.4.10, Compose Multiplatform to 1.12.0, AndroidX Lifecycle to 2.11.0 and kotlinx-collections-immutable to 0.5.2
- Build tooling: updated Gradle to 9.7.1 and AGP to 9.4.0

## [1.1.0] (2026-07-22)

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

[unreleased]: https://github.com/RedMadRobot/Konfeature/compare/v1.2.0...main
[1.2.0]: https://github.com/RedMadRobot/Konfeature/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/RedMadRobot/Konfeature/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/RedMadRobot/Konfeature/compare/v0.1.0...v1.0.0
