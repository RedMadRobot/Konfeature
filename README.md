# Konfeature

[![Version](https://img.shields.io/maven-central/v/com.redmadrobot.konfeature/konfeature?style=flat-square)][mavenCentral]
[![Build Status](https://img.shields.io/github/actions/workflow/status/RedMadRobot/konfeature/main.yml?branch=main&style=flat-square)][ci]
[![License](https://img.shields.io/github/license/RedMadRobot/Konfeature?style=flat-square)][license]


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

- [Installation](#installation)
- [Usage](#usage)
  - [FeatureConfig](#featureconfig)
  - [FeatureSource](#featuresource)
  - [SourceSelectionStrategy](#sourceselectionstrategy)
  - [Interceptor](#interceptor)
  - [Logger](#logger)
  - [Spec](#spec)
  - [Ordering](#ordering)
- [Contributing](#contributing)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## Installation

Add the dependency:

```groovy
repositories {
    mavenCentral()
}

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

## Contributing

Merge requests are welcome.  
For major changes, please open an issue first to discuss what you would like to change.

[mavenCentral]: https://central.sonatype.com/artifact/com.redmadrobot.konfeature/konfeature
[ci]: https://github.com/RedMadRobot/Konfeature/actions?query=branch%3Amain
[license]: ./LICENSE
