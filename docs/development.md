# Development

## Local setup

### Project import
The project should work in IntelliJ IDEA 2018 and Android Studio 3.2/3.3.
Current development is being done on IntelliJ IDEA 2019 EAP.

0. `ANDROID_HOME` should be set to an Android SDK root folder.  
   This is required because the tests build full Android projects.
   For more info see [Official Documentation on environment variables](https://developer.android.com/studio/command-line/variables).
1. First make sure it runs successfully from terminal with `./gradlew tests`.
   * Check failure reasons (in JUnit report) for things to fix.
   * This helps to rule out trivial problems during import.
2. After it builds successfully, import the root `build.gradle.kts` into IDE.

*Please report import problems if you encounter them, even if you fixed them, so I can add it here.*

### Debugging from IDE
See [test/README.md](../test/README.md).

### Running tests from IDE
See [test/README.md](../test/README.md).

### Using the `-SNAPSHOT` from a local repo
It's possible to deploy a snapshot into the current user's `.m2` folder.
This is useful for testing on external local projects.

#### Publish
```console
gradlew publishToMavenLocal
```

#### Consume
```
buildscript {
	repositories {
		mavenLocal() // make sure it's first
	}
}
```
and then business as usual (`buildscript { dependencies { classpath "..."` etc. see README.md).


## Using the `-SNAPSHOT` from a local build
This bypasses Gradle's dependency management and uses jars directly. The drawback is that each transitive dependency has to be manually added.

Add in root `build.gradle`:
```groovy
buildscript {
	repositories {
		mavenCentral()
		def repoRoot = file($/P:\projects\workspace\net.twisterrob.gradle-quality/$).toURI()
		ivy {
			url = repoRoot
			patternLayout {
				artifact '[artifact]/build/libs/[artifact]-[revision](-[classifier]).[ext]'
			}
		}
		ivy {
			url = repoRoot
			patternLayout {
				artifact 'checkers/[artifact]/build/libs/[artifact]-[revision](-[classifier]).[ext]'
			}
		}
	}
	dependencies {
		configurations.classpath.resolutionStrategy.cacheChangingModulesFor 0, 'seconds' // -SNAPSHOT
		def VERSION_QUALITY='0.2-SNAPSHOT'
		classpath "net.twisterrob.gradle:twister-quality:${VERSION_QUALITY}"
		classpath "net.twisterrob.gradle:twister-quality-common:${VERSION_QUALITY}"
		classpath "net.twisterrob.gradle:twister-quality-checkstyle:${VERSION_QUALITY}"
		classpath "net.twisterrob.gradle:twister-quality-pmd:${VERSION_QUALITY}"
		classpath "se.bjurr.violations:violations-lib:1.50"
		// ... maybe more transitive dependencies
	}
}
```
To try it out, add `printViolationCounts` from README.md and run `gradlew checkstyleEach :printViolationCounts`.


## Details and background


### Project structure
The project is using a modular architecture to reduce coupling and so it's possible to use only part of the project.

| Module (location): summary | Distributed Artifact,<br>Gradle Plugin ID,<br>JVM Package |
| --- | --- |
| **quality** ([`/quality`](../quality)):<br>All quality plugins bundled in one.<br> |`classpath 'net.twisterrob.gradle:twister-quality:+'`<br>`apply plugin: 'net.twisterrob.quality'`<br>`import net.twisterrob.gradle.quality;` |
| **common** ([`/common`](../common)):<br>Shared classes between checkers.<br>_Not to be consumed directly._ | `classpath 'net.twisterrob.gradle:twister-quality-common:+'`<br>`// apply plugin: N/A`<br>`import net.twisterrob.gradle.common;` |
| **checkstyle** ([`/checkers/checkstyle`](../checkers/checkstyle)):<br>Checkstyle setup plugin for Gradle. | `classpath 'net.twisterrob.gradle:twister-quality-checkstyle:+'`<br>`apply plugin: 'net.twisterrob.checkstyle'`<br>`import net.twisterrob.gradle.checkstyle;` |
| **pmd** ([`/checkers/pmd`](../checkers/pmd)):<br>PMD setup plugin for Gradle. | `classpath 'net.twisterrob.gradle:twister-quality-pmd:+'`<br>`apply plugin: 'net.twisterrob.pmd'`<br>`import net.twisterrob.gradle.pmd;` |
| **test** ([`/test`](../test)):<br>[Gradle test plugin and resources.](../test/README.md) | `classpath 'net.twisterrob.gradle:twister-gradle-test:+'`<br>`apply plugin: 'net.twisterrob.gradle.test'`<br>`import net.twisterrob.gradle.test;` |
| **plugin** ([`/plugin`](../plugin)):<br>[Gradle Android plugin conventions.](../plugin/README.md) | `classpath 'net.twisterrob.gradle:twister-convention-plugins:+'`<br>`apply plugin: 'net.twisterrob.android-app'`<br>`apply plugin: 'net.twisterrob.root'`<br>`import net.twisterrob.gradle.android;` |


## Used languages
Most of the code is written in Kotlin, some in Groovy to test the integration with traditional Gradle, and some in Java to test interop (mostly Generics).

> Originally Groovy was the main language with `@CompileStatic` enabled so there's some type checking during compilation. Still after a successful `groovyc` compilation it was possible to get unrunnable invalid class files, and the amount of time spent on finding workarounds and helping the static compiler to infer the types was more than the ease and fun of converting everything to Kotlin.
