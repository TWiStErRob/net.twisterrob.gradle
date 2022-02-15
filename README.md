[![Continuous Integration](https://github.com/TWiStErRob/net.twisterrob.gradle/actions/workflows/CI.yml/badge.svg)](https://github.com/TWiStErRob/net.twisterrob.gradle/actions/workflows/CI.yml)
[![Releases](https://img.shields.io/github/v/release/twisterrob/net.twisterrob.gradle)](
https://github.com/TWiStErRob/net.twisterrob.gradle/releases)
[![Maven Central](https://img.shields.io/maven-central/v/net.twisterrob.gradle/twister-quality)](
https://search.maven.org/search?q=g:net.twisterrob.gradle)
[![Licence](https://img.shields.io/github/license/twisterrob/net.twisterrob.gradle)](
https://github.com/TWiStErRob/net.twisterrob.gradle/blob/master/LICENCE)
[![Twitter](https://img.shields.io/twitter/follow/twisterrob?style=social)](
https://twitter.com/twisterrob)

# Gradle Quality plugins

Plugins that configure the built-in plugins with saner defaults (to be documented).
 * CheckStyle
 * PMD
 * Android Lint

Current goal is to make Android support better, because of build types and flavors.

---

For details on what was changed in different versions, see [CHANGELOG](CHANGELOG.md).

## Compatibility

Android Gradle Plugin 3.1.4 — 7.0.x on Gradle 4.9 — 7.4.x as listed in [AGP's compatibility guide](https://developer.android.com/studio/releases/gradle-plugin#updating-gradle).

| AGP →<br/>Gradle ↓ |   3.1.x   |  3.2.x  |  3.3.x  |  3.4.x  |  3.5.x   |  3.6.x   |  4.0.x   |  4.1.x   |  4.2.x   |  7.0.x   | 7.1.x | 7.2.x |
|:-------------------|:---------:|:-------:|:-------:|:-------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:-----:|:-----:|
| 4.4 - 4.8.1        | 0.5 - 0.7 |   ^1    |    ❌    |    ❌    |    ❌     |    ❌     |    ❌     |    ❌     |    ❌     |    ❌     |   ❌   |   ❌   |
| 4.9 - 4.10         |  0.8 - ∞  | 0.7 - ∞ |    ❌    |    ❌    |    ❌     |    ❌     |    ❌     |    ❌     |    ❌     |    ❌     |   ❌   |   ❌   |
| 4.10.1 - 5.1       |     ❓     | 0.8 - ∞ | 0.8 - ∞ |    ❌    |    ❌     |    ❌     |    ❌     |    ❌     |    ❌     |    ❌     |   ❌   |   ❌   |
| 5.1.1 - 5.4        |     ❓     |    ❓    | 0.9 - ∞ | 0.9 - ∞ |    ❌     |    ❌     |    ❌     |    ❌     |    ❌     |    ❌     |   ❌   |   ❌   |
| 5.4.1 - 5.6.3      |     ❓     |    ❓    |    ❓    | 0.9 - ∞ |   sup.   |    ❌     |    ❌     |    ❌     |    ❌     |    ❌     |   ❌   |   ❌   |
| 5.6.4              |     ❓     |    ❓    |    ❓    |    ❓    | 0.10 - ∞ | 0.10 - ∞ |    ❌     |    ❌     |    ❌     |    ❌     |   ❌   |   ❌   |
| 6.0 - 6.1          |     ❓     |    ❓    |    ❓    |    ❓    | 0.11 - ∞ | 0.11 - ∞ |    ❌     |    ❌     |    ❌     |    ❌     |   ❌   |   ❌   |
| 6.1.1 - 6.4.1      |     ❓     |    ❓    |    ❓    |    ❓    | 0.11 - ∞ | 0.11 - ∞ | 0.11 - ∞ |    ❌     |    ❌     |    ❌     |   ❌   |   ❌   | 
| 6.5 - 6.7          |     ❓     |    ❓    |    ❓    |    ❓    | 0.11 - ∞ | 0.11 - ∞ | 0.11 - ∞ | 0.11 - ∞ |    ❌     |    ❌     |   ❌   |   ❌   | 
| 6.7.1 - 6.9.1      |     ❓     |    ❓    |    ❓    |    ❓    |    ❓     |    ❓     |    ❓     | 0.11 - ∞ | 0.11 - ∞ |    ❌     |   ❌   |   ❌   | 
| 7.0 - 7.3.2        |     ❓     |    ❓    |    ❓    |    ❓    |    ❓     |    ❓     |    ❓     |    ❓     | 0.13 - ∞ | 0.13 - ∞ |  N/A  |  N/A  |

 * ❓ = not sure if it's supported by AGP, never tested.
 * ❌ = incompatible based on AGP compatibility.
 * sup. = supported based on AGP compatibility, but not tested.
 * N/A = not available yet
 * ∞ = latest version
 * ^1 = Gradle 4.4 — 4.9 is not supported, because it's hard to backport the lazy task configuration APIs.
 * ^2 = Convention plugins only support Android Gradle Plugin 4.0.0 — 4.2.2 on Gradle 6.1.1+, because it's really hard to backport all the features to 3.x with no need for this.

## Quick setup
There are different ways to use a Gradle plugin, choose your poison below.
<details>
	<summary>normal build.gradle(.kts) (<code>buildscript</code>)</summary>

```gradle
buildscript {
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath("net.twisterrob.gradle:twister-quality:x.y")
	}
}
// Kotlin
apply(plugin = "net.twisterrob.quality")
// Groovy
apply plugin: "net.twisterrob.quality"
```

</details>

<details>
	<summary>buildSrc classpath</summary>

#### `buildSrc/build.gradle(.kts)`

```gradle
repositories {
	mavenCentral()
}
dependencies {
	implementation("net.twisterrob.gradle:twister-quality:x.y")
}
```

#### `build.gradle(.kts)`

```gradle
// Kotlin
apply(plugin = "net.twisterrob.quality")
// Groovy
apply plugin: "net.twisterrob.quality"
```

</details>

<details>
	<summary><code>plugins</code> block and settings.gradle(.kts)</summary>

See it live at [SNAPSHOT example](docs/examples/snapshot/settings.gradle.kts).

```gradle
plugins {
	id("net.twisterrob.quality") version "x.y"
}
```

```gradle
pluginManagement {
	repositories {
		mavenCentral()
	}
	resolutionStrategy {
		eachPlugin {
			when (requested.id.id) {
				"net.twisterrob.quality" ->
					useModule("net.twisterrob.gradle:twister-quality:${requested.version}")
			}
		}
	}
}
```

</details>

For more, see the [examples](docs/examples) folder.

## Features

### HTML violation report

There's a built-in HTML report that gathers all the results from all the modules into a single HTML file.

```shell
gradlew :violationReportHtml
```

### Console violation report

There's a built-in console report that gathers all the results from all the modules and outputs results to the console.

```shell
gradlew :violationReportConsole
```

### Count violation report to file

It just saves the number of violations into a file. Good for automation.

```shell
gradlew :violationCountFile
```

### Fail the build on violation

It just fails if there are violations.

```shell
gradlew :validateViolations
```

### Root project test report

Gathers results from submodules and fails if there were errors.

```groovy
gradlew :testReport
```

Note: this changes the `:*:test` test tasks to not fail so a whole project encompassing report can be generated.

### Global finalizer `:lint` task
Depends on all the other lints and shows a summary of failures to reduce the need to scroll/scan the build logs.
If invoked explicitly as `gradlew :lint` it'll fail, otherwise (e.g. `gradlew lint`) it just silently adds itself to the list of `lint` tasks along with the others and prints the summary at the end.

To disable:

```gradle
afterEvaluate { tasks.named("lint").configure { it.enabled = false } }
```

## Contributions, custom builds

See [development.md](docs/development.md) on how to set this project up.
