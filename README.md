# Konfeature

<img width="1560" height="877" alt="75fc26ed9b4a25dc2010f846dae2d792" src="https://github.com/user-attachments/assets/218c27e6-0d06-4c25-bbaa-796343d4e7d5" />

[![Version](https://img.shields.io/maven-central/v/com.redmadrobot.konfeature/konfeature?style=flat-square)][mavenCentral]
[![Build Status](https://img.shields.io/github/actions/workflow/status/RedMadRobot/konfeature/main.yml?branch=main&style=flat-square)][ci]
[![License](https://img.shields.io/github/license/RedMadRobot/Konfeature?style=flat-square)][license]
[![Android](https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white)](#)
[![iOS](https://img.shields.io/badge/iOS-000000?style=flat-square&logo=apple&logoColor=white)](#)
[![JVM](https://img.shields.io/badge/JVM-007396?style=flat-square&logo=java&logoColor=white)](#)

**Konfeature** is a powerful **Kotlin Multiplatform** library for managing remote configuration in your applications. It provides a clean, declarative API for working with feature flags and configuration elements across Android, iOS, and JVM platforms.

Working with remote configuration has become a standard part of the development process for almost any application. Depending on the complexity of the application, several requirements for such functionality may arise, including:
- convenient syntax for declaring configuration elements
- the ability to separate configuration into different files for different features
- the ability to make the configuration local-only during active feature development
- support for multiple data sources for remote config
- the ability to view a list of all configurations and modify their values for debugging purposes
- logging the value and its source when accessing the configuration, as well as logging non-critical errors

We have made every effort to meet all these requirements in the development of Konfeature.

---
<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->

- [Supported Platforms](#supported-platforms)
- [Installation](#installation)
- [Usage](#usage)
  - [FeatureConfig](#featureconfig)
  - [FeatureSource](#featuresource)
  - [SourceSelectionStrategy](#sourceselectionstrategy)
  - [Interceptor](#interceptor)
  - [Logger](#logger)
  - [Spec](#spec)
  - [Ordering](#ordering)
- [Debug UI (konfeature-ui)](#debug-ui-konfeature-ui)
  - [What problem it solves](#what-problem-it-solves)
  - [Installation](#installation-1)
  - [Usage](#usage-1)
  - [Theming](#theming)
  - [No-op implementation (konfeature-ui-noop)](#no-op-implementation-konfeature-ui-noop)
- [Contributing](#contributing)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## Supported Platforms

Konfeature is a **Kotlin Multiplatform** library with support for:

| Platform | Status | Targets |
|----------|--------|---------|
| **Android** | ✅ Fully Supported | JVM (via Kotlin/JVM) |
| **iOS** | ✅ Fully Supported | arm64, simulator arm64 |
| **JVM** | ✅ Fully Supported | Java/Kotlin applications |

## Installation

### Add Maven Central Repository

```groovy
repositories {
    mavenCentral()
}
```

### Add Dependency

**For Gradle (Kotlin Multiplatform Project):**

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.redmadrobot.konfeature:konfeature:<version>")
        }
    }
}
```

**For Gradle (Single Platform):**

```groovy
dependencies {
    implementation("com.redmadrobot.konfeature:konfeature:<version>")
}
```

## Usage

### FeatureConfig

Defines a set of configuration elements, where each element is defined using a delegate.
There are two types of delegates:
- `by toggle(...)` - used for elements of type `Boolean`
- `by value(...)` - used for elements of any other type

```kotlin
class ProfileFeatureConfig : FeatureConfig(
    name = "profile_feature_config",
    description = "Config of features for profile usage"
) {
    val isProfileFeatureEnabled: Boolean by toggle(
        key = "profile_feature",
        description = "show profile entry point for user",
        defaultValue = false,
    )

    val profileFeatureTitle: String by value(
        key = "profile_feature_title",
        description = "title of profile entry point button",
        defaultValue = "Feature number nine",
        sourceSelectionStrategy = SourceSelectionStrategy.Any
    )

    val profileButtonAppearDuration: Long by value(
        key = "profile_button_appear_duration",
        description = "duration of profile button appearing in ms",
        defaultValue = 200,
        sourceSelectionStrategy = SourceSelectionStrategy.Any
    )
}
```

The configuration requires specifying:
- `name` - the name of the configuration
- `description` - a detailed description of the configuration

Each configuration element requires specifying:
- `key` - used to retrieve the value of the element from a `Source`
- `description` - a detailed description of the element
- `defaultValue` - used if the value cannot be found in a `Source`
- `sourceSelectionStrategy` - the strategy for selecting a `Source` using [SourceSelectionStrategy](#sourceselectionstrategy)

After that, you need to register the configuration in `Konfeature`:

```kotlin
val profileFeatureConfig: FeatureConfig = ProfileFeatureConfig()

val konfeatureInstance = konfeature {
    register(profileFeatureConfig) 
}
```

>Similarly, you can add multiple configurations, for example, for each module, when organizing multi-modularity by features.

### FeatureSource

An abstraction over the value source for configuration elements.

```kotlin
public interface FeatureSource {

    public val name: String

    public fun get(key: String): Any?
}
```
- `name` - source name
- `get(key: String)` - logic for getting values by `key`  

Example implementation based on `FirebaseRemoteConfig`:

```kotlin
class FirebaseFeatureSource(
    private val remoteConfig: FirebaseRemoteConfig
) : FeatureSource {
    
    override val name: String = "FirebaseRemoteConfig"

    override fun get(key: String): Any? {
        return remoteConfig
            .getValue(key)
            .takeIf { source == FirebaseRemoteConfig.VALUE_SOURCE_REMOTE }
            ?.let { value: FirebaseRemoteConfigValue ->
                value.getOrNull { asBoolean() }
                    ?: value.getOrNull { asString() }
                    ?: value.getOrNull { asLong() }
                    ?: value.getOrNull { asDouble() }
            }
    }
        
    private fun FirebaseRemoteConfigValue.getOrNull(
        getter: FirebaseRemoteConfigValue.() -> Any?
    ): Any? {        
        return try {
            getter()    
        } catch (error: IllegalArgumentException) {
            null
        }        
    }
}
```
After that, you need to add the `Source` in `Konfeature`:

```kotlin
val profileFeatureConfig: FeatureConfig = ProfileFeatureConfig()
val source: FeatureSource = FirebaseFeatureSource(remoteConfig)

val konfeatureInstance = konfeature {
    addSource(source)
    register(profileFeatureConfig) 
}
```

>Similarly, you can add multiple sources, for example, Huawei AppGallery, RuStore, or your own backend.

### SourceSelectionStrategy

You can configure the retrieval of an element's value from the source more flexibly by using the `sourceSelectionStrategy` parameter:

```kotlin
val profileFeatureTitle: String by value(
    key = "profile_feature_title",
    description = "title of profile entry point button",
    defaultValue = "Feature number nine",
    sourceSelectionStrategy = SourceSelectionStrategy.Any
)
```

Where `sourceSelectionStrategy` filters the available data sources.

```kotlin
public fun interface SourceSelectionStrategy {

    public fun select(names: Set<String>): Set<String>

    public companion object {
        public val None: SourceSelectionStrategy = SourceSelectionStrategy { emptySet() }
        public val Any: SourceSelectionStrategy = SourceSelectionStrategy { it }

        public fun anyOf(vararg sources: String): SourceSelectionStrategy = SourceSelectionStrategy { sources.toSet() }
    }
}
```

The `select(...)` method receives a list of available `Source` names and returns a list of sources from which the configuration element can retrieve a value.

For most scenarios, predefined implementations will be sufficient:
- `SourceSelectionStrategy.None` - prohibits taking values from any source, i.e., the value specified in `defaultValue` will always be used
- `SourceSelectionStrategy.Any` - allows taking values from any source
- `SourceSelectionStrategy.anyOf("Source 1", ... ,"Source N")` - allows taking values from the specified list of sources

> [!IMPORTANT]
> By default, `SourceSelectionStrategy.None` is used!

### Interceptor

Allows intercepting and overriding the value of the element.

```kotlin
public interface Interceptor {

    public val name: String

    public fun intercept(valueSource: FeatureValueSource, key: String, value: Any): Any?
}
```

- `name` - the name of the interceptor
- `intercept(valueSource: FeatureValueSource, key: String, value: Any): Any?` - called when accessing the element with `key` and `value` from `valueSource(Source(<name>), Interceptor(<name>), Default)`, and returns its new value or `null` if it doesn't change

Example of implementation based on `DebugPanelInterceptor`:

```kotlin
class DebugPanelInterceptor : Interceptor {

    private val values = mutableMapOf<String, Any>()

    override val name: String = "DebugPanelInterceptor"

    override fun intercept(valueSource: FeatureValueSource, key: String, value: Any): Any? {
        return values[key]
    }

    fun setFeatureValue(key: String, value: Any) {
        values[key] = value
    }

    fun removeFeatureValue(key: String) {
        values.remove(key)
    }
}
```

After that, you need to add the `Interceptor` in `Konfeature`:

```kotlin
val profileFeatureConfig: FeatureConfig = ProfileFeatureConfig()
val source: FeatureSource = FirebaseFeatureSource(remoteConfig)
val debugPanelInterceptor: Interceptor = DebugPanelInterceptor()

val konfeatureInstance = konfeature {
    addSource(source)
    register(profileFeatureConfig)
    addInterceptor(debugPanelInterceptor)
}
```

>Similarly, you can add multiple interceptors.

### Logger

```kotlin
public interface Logger {

    public fun log(severity: Severity, message: String)

    public enum class Severity {
        WARNING, INFO
    }
}
```

The following events are logged:

- key, value, and its source when requested
>Get value 'true' by key 'profile_feature' from 'Source(name=FirebaseRemoteConfig)'
- `Source` or `Interceptor` returns an unexpected type for `key`
>Unexpected value type for 'profile_button_appear_duration': expected type is 'kotlin.Long', but value from 'Source(name=FirebaseRemoteConfig)' is 'true' with type 'kotlin.Boolean'

Example of implementation based on `Timber`:

```kotlin
class TimberLogger: Logger {
    
    override fun log(severity: Severity, message: String) {
        if (severity == INFO) {
            Timber.tag(TAG).i(message)    
        } else if (severity == WARNING) {
            Timber.tag(TAG).w(message)
        }
    }
    
    companion object {
        private const val TAG = "Konfeature"
    }
}
```

After that, you need to add the `Logger` in `Konfeature`:

```kotlin
val profileFeatureConfig: FeatureConfig = ProfileFeatureConfig()
val source: FeatureSource = FirebaseFeatureSource(remoteConfig)
val debugPanelInterceptor: Interceptor = DebugPanelInterceptor()
val logger: Logger = TimberLogger()

val konfeatureInstance = konfeature {
    addSource(source)
    register(profileFeatureConfig)
    addInterceptor(debugPanelInterceptor)
    setLogger(logger)
}
```

### Spec

Konfeature contains information about all registered `FeatureConfig` in the form of `spec`:

```kotlin
public interface Konfeature {

    public val spec: List<FeatureConfigSpec>

    public fun <T : Any> getValue(spec: FeatureValueSpec<T>): FeatureValue<T>
}
```

This allows you to obtain information about added configurations as well as the current value of each element:

```kotlin
val konfeatureInstance = konfeature {...}

val featureConfigSpec = konfeatureInstance.spec[0]
val featureSpec = featureConfigSpec.values[0]
val featureValue = konfeatureInstance.getValue(featureSpec)
```
>  This can be useful for use in the DebugPanel

## Ordering
The value of the configuration element is determined in the following order:

- `defaultValue` and `Default` source are assigned.
- Using `sourceSelectionStrategy`, a list of `Sources` from which a value can be requested is determined.
- Search the list of `Sources` in the order they were added to `Konfeature`, **stopping at the first occurrence** of the element by `key`.
  Upon successful search, the value from `Source` is assigned with `Source(name=SourceName)` source.
- Search the list of `Interceptors` in the order they were added to `Konfeature`.
  If `Interceptor` returns a value other than `null`, this value is assigned with `Interceptor(name=InterceptorName)` source.

## Debug UI (konfeature-ui)

`konfeature-ui` is a separate, optional **Compose Multiplatform** module that ships a ready-made debug
panel for Konfeature. Add it on top of the core `konfeature` dependency when you want a screen to
inspect and override feature values at runtime.

<video src="https://github.com/user-attachments/assets/5e12c5f7-7bab-4856-b536-c52d0145c44c" width="320" controls>
  <a href="https://github.com/user-attachments/assets/5e12c5f7-7bab-4856-b536-c52d0145c44c">Watch the konfeature-ui demo</a>
</video>

| Module | Artifact | Targets | Purpose |
|--------|----------|---------|---------|
| **konfeature-ui** | `com.redmadrobot.konfeature:konfeature-ui` | Android, iOS | Debug panel + persisted overrides |
| **konfeature-ui-noop** | `com.redmadrobot.konfeature:konfeature-ui-noop` | Android, iOS | API-compatible no-op for release builds |

### What problem it solves

The core library already exposes everything needed to build a debug panel — [`spec`](#spec) for the
list of registered configs, [`getValue`](#spec) for the current value and its source, and the
[`Interceptor`](#interceptor) mechanism for overriding values. Wiring all of this together (an
interceptor, persistence of overrides across app restarts, and a Compose screen) is repetitive
boilerplate that every project ends up re-implementing.

`konfeature-ui` provides that out of the box:

- **`KonfeatureDebugPanel`** — a Compose Multiplatform screen listing every registered `FeatureConfig`,
  grouped and collapsible, with search (by key, description or config name), the current value, and the
  source it came from (`Default` / a `Source` / the debug interceptor). Each config header shows how many
  of its values are currently overridden, an overridden value can be reset individually, and toolbar
  actions let you refresh, collapse all groups, or reset all overrides at once.
- **`KonfeatureDebugInterceptor`** — an `Interceptor` that applies the overrides made in the screen,
  so a value changed in the panel is reflected everywhere the config is read.
- **`KonfeatureDebugStore`** — persists overrides to disk (via DataStore) so they survive app
  restarts, and exposes them as a `StateFlow` for the screen and a synchronous `currentValue(key)`
  read for the interceptor. Overrides can be removed one at a time (`resetValue(key)`) or all at once
  (`resetAll()`).

Booleans are toggled inline in the screen. For non-boolean values the screen does not edit in place —
it reports the tapped value via `onValueClick(info)`, where `info` is a `KonfeatureValueInfo` carrying
everything needed to build an editor: the `key`, `configName`, `description`, the declared `type`
(a `KonfeatureValueType`), the `currentValue` and `defaultValue`, the `sourceName` it resolved from,
and whether it `isOverridden`. Open your own editor pre-filled with that value and write the result
back with `store.setValue(key, newValue)`.

> [!IMPORTANT]
> **Only primitive override types are persisted:** `Boolean`, `Int`, `Long`, `Float`, `Double` and
> `String`. `KonfeatureDebugStore.setValue` accepts `Any`, but values of other types (enums, data
> classes, lists, etc.) **cannot be serialized**, so `setValue` **throws `IllegalArgumentException`**
> for them and leaves the current overrides untouched — this prevents an override that would apply in
> memory only to silently vanish on the next load. Values of such a type are surfaced as
> `KonfeatureValueType.OTHER` and shown read-only in the panel (their rows are not clickable). If you
> need to override a complex type, model it as one of the supported primitives (e.g. store an enum by
> its `name` and map it back in your config).

### Installation

`konfeature-ui` depends on (`api`) the core `konfeature` module, so adding it pulls the core in
transitively.

**For a Kotlin Multiplatform project:**

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.redmadrobot.konfeature:konfeature-ui:<version>")
        }
    }
}
```

**For a pure Android (non-KMP) project:**

Being a KMP library with an Android target, `konfeature-ui` is consumable from a plain Android Gradle
project — Gradle resolves its Android variant automatically:

```kotlin
dependencies {
    implementation("com.redmadrobot.konfeature:konfeature-ui:<version>")
}
```

`KonfeatureDebugPanel` is a `@Composable`, so the app must have Jetpack Compose set up (Compose
Multiplatform builds on top of it on Android) to render it.

> Unlike the core library, `konfeature-ui` targets **Android and iOS** only (it builds on Compose
> Multiplatform), so there is no plain-JVM (desktop/server) artifact — but Android is fully supported.

### Usage

1. Create the store once on startup. `KonfeatureDebugStore.create(...)` constructs the store **and**
   loads any persisted overrides before returning, so callers can't forget to load it. It is a
   `suspend` function (the initial load reads from disk), so call it from a coroutine scope during
   startup. Hold a single instance (DI singleton or shared `object`) and reuse it for both the
   interceptor and the screen.

   The `path` is platform-specific — on Android use `context.filesDir`, on iOS use `NSDocumentDirectory`.

   ```kotlin
   // inside a coroutine / suspend context
   val store = KonfeatureDebugStore.create(
       path = context.filesDir.resolve("konfeature_debug.preferences_pb").absolutePath,
       logger = konfeatureLogger, // optional, reuses Konfeature's Logger
   )
   ```

2. Register the interceptor backed by the store. Add it like any other [interceptor](#interceptor);
   until the store is loaded it reports no overrides, so values fall through to the source/default.

   ```kotlin
   val interceptor = KonfeatureDebugInterceptor(store)

   val konfeature = konfeature {
       addSource(source)
       register(profileFeatureConfig)
       addInterceptor(interceptor)
   }
   ```

3. Show the screen from a debug-only entry point, passing the same `konfeature` and `store`:

   ```kotlin
   KonfeatureDebugPanel(
       konfeature = konfeature,
       store = store,
       onValueClick = { info ->
           // Non-boolean value tapped. `info` (KonfeatureValueInfo) carries info.key,
           // info.currentValue, info.type, etc. Open your own editor pre-filled with the
           // current value, then persist the result:
           // store.setValue(info.key, newValue)
       },
   )
   ```

### Theming

The panel ships with a default look and works out of the box: if it is not wrapped in a
`KonfeatureTheme`, it installs one for you, picking a light or dark palette based on
`isSystemInDarkTheme()`. To brand it, wrap `KonfeatureDebugPanel` in your own `KonfeatureTheme` and
pass a customized `KonfeatureColors`:

```kotlin
KonfeatureTheme(
    colors = lightKonfeatureColors(
        accent = Color(0xFF00695C),
        sourceRemote = Color(0xFFB26A00),
        sourceDebug = Color(0xFF2E7D32),
    ),
) {
    KonfeatureDebugPanel(konfeature = konfeature, store = store)
}
```

`KonfeatureColors` is the set of semantic color tokens the panel reads (`background`, `surface`,
`surfaceHighlight`, `stroke`, `contentPrimary` / `contentSecondary` / `contentTertiary`, `accent`,
`onAccent`, and `sourceRemote` / `sourceDebug` for the value-source labels). Start from
`lightKonfeatureColors(...)` or `darkKonfeatureColors(...)` and override individual slots via named
arguments to keep the remaining defaults. `KonfeatureTheme` also
maps the palette onto a Material 3 `ColorScheme`, so Material components rendered inside the panel
follow the same light/dark appearance.

> [!NOTE]
> Only colors are customizable. Typography and shapes are internal to the panel.

### No-op implementation (konfeature-ui-noop)

`konfeature-ui-noop` exposes the **same `KonfeatureDebugStore` and `KonfeatureDebugInterceptor` API**
(same package, same class names and signatures) but does nothing: the store holds no overrides and
performs no I/O, and the interceptor always returns `null`. It has no Compose dependency and does not
contain `KonfeatureDebugPanel`.

This lets you keep the store/interceptor wiring in your shared production code and swap the
implementation per build type, so the debug tooling and its DataStore/persistence cost are stripped
from release builds:

```kotlin
dependencies {
    debugImplementation("com.redmadrobot.konfeature:konfeature-ui:<version>")
    releaseImplementation("com.redmadrobot.konfeature:konfeature-ui-noop:<version>")
}
```

> [!IMPORTANT]
> `KonfeatureDebugPanel` exists **only** in `konfeature-ui` — there is no no-op counterpart for it.
> Only `KonfeatureDebugStore` and `KonfeatureDebugInterceptor` are swappable between the two modules.
> Keep any reference to `KonfeatureDebugPanel` in a debug-only source set / module: the store and
> interceptor compile against both modules, but the screen compiles against `konfeature-ui` only, so a
> release build wired to `konfeature-ui-noop` will not see it.

## Contributing

Merge requests are welcome.  
For major changes, please open an issue first to discuss what you would like to change.

[mavenCentral]: https://central.sonatype.com/artifact/com.redmadrobot.konfeature/konfeature
[ci]: https://github.com/RedMadRobot/Konfeature/actions?query=branch%3Amain
[license]: ./LICENSE
