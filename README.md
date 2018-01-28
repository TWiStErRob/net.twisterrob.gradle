# Gradle Quality plugins
Plugins that configure the built-in plugins with saner defaults.

```gradle
buildscript {
	dependencies {
		classpath 'net.twisterrob.gradle:twister-quality'
	}
}
apply plugin: 'net.twisterrob.quality'
// above includes the following:
// (but you can cherry-pick them one by one if you don't apply all)
//apply plugin: 'net.twisterrob.checkstyle'
//apply plugin: 'net.twisterrob.pmd'
```

Example to use all plugins and print results:
```groovy
allprojects {
	apply plugin: 'net.twisterrob.quality'
}

task('printViolationCounts', type: net.twisterrob.gradle.quality.ValidateViolationsTask) {
	action = {net.twisterrob.gradle.common.grouper.Grouper.Start<se.bjurr.violations.lib.model.Violation> results ->
		results.by.parser.module.variant.group().each { checker, byModule -> 
			println "\t${checker}"
			byModule.each {module, byVariant ->
				println "\t\t${module}:"
				byVariant.each {variant, violations ->
					println "\t\t\t${variant}: ${violations.size()}"
				}
			}
		}
	}
}
```

### Root project test report:
Gathers results from submodules and fails if there were errors.
```groovy
task('tests', type: net.twisterrob.gradle.quality.tasks.GlobalTestFinalizerTask)
```

## Development

### Structure

| Module (location): summary | Distributed Artifact,<br>Gradle Plugin ID,<br>JVM Package |
| --- | --- |
| **quality** ([`/quality`](quality)):<br>All quality plugins bundled in one.<br> |`classpath 'net.twisterrob.gradle:twister-quality:+'`<br>`apply plugin: 'net.twisterrob.quality'`<br>`import net.twisterrob.gradle.quality;` |
| **common** ([`/common`](common)):<br>Shared classes between checkers.<br>_Not to be consumed directly._ | `classpath 'net.twisterrob.gradle:twister-quality-common:+'`<br>`// apply plugin: N/A`<br>`import net.twisterrob.gradle.common;` |
| **checkstyle** ([`/checkers/checkstyle`](checkers/checkstyle)):<br>Checkstyle setup plugin for Gradle. | `classpath 'net.twisterrob.gradle:twister-quality-checkstyle:+'`<br>`apply plugin: 'net.twisterrob.checkstyle'`<br>`import net.twisterrob.gradle.checkstyle;` |
| **pmd** ([`/checkers/pmd`](checkers/pmd)):<br>PMD setup plugin for Gradle. | `classpath 'net.twisterrob.gradle:twister-quality-pmd:+'`<br>`apply plugin: 'net.twisterrob.pmd'`<br>`import net.twisterrob.gradle.pmd;` |
| **test** ([`/test`](test)):<br>[Gradle test plugin and resources.](test/README.md) | `classpath 'net.twisterrob.gradle:twister-gradle-test:+'`<br>`apply plugin: 'net.twisterrob.gradle.test'`<br>`import net.twisterrob.gradle.test;` |

### Languages
Most of the code is written in Kotlin, some in Groovy to test the integration with traditional Gradle, and some in Java to test interop (mostly Generics).
 
> Originally Groovy was the main language with `@CompileStatic` enabled so there's some type checking during compilation. Still after a successful `groovyc` compilation it was possible to get unrunnable invalid class files, and the amount of time spent on finding workarounds and helping the static compiler to infer the types was more than the ease and fun of converting everything to Kotlin.

### Project

1. Make sure it runs successfully from terminal with `./gradlew test`.  
   Check failure reason (JUnit report) for things to fix.
2. After it builds successfully it's ok to import the root `build.gradle` into IntelliJ IDEA/Android Studio.
3. For running and debugging info see [test/README.md](test/README.md)

### Using the `-SNAPSHOT` from a local build

Add in root `build.gradle`:
```groovy
buildscript {
	repositories {
		jcenter()
		def repoRoot = file($/P:\projects\workspace\net.twisterrob.gradle-quality/$).toURI()
		ivy {
			url = repoRoot
			layout('pattern') {
				artifact '[artifact]/build/libs/[artifact]-[revision](-[classifier]).[ext]'
			}
		}
		ivy {
			url = repoRoot
			layout('pattern') {
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
	}
}
```
Add `printViolationCounts` from above and run `gradlew checkstyleEach :printViolationCounts`.

## Useful articles
 * https://proandroiddev.com/configuring-android-project-static-code-analysis-tools-b6dd83282921
 * https://medium.com/mindorks/static-code-analysis-for-android-using-findbugs-pmd-and-checkstyle-3a2861834c6a
 * http://vincentbrison.com/2014/07/19/how-to-improve-quality-and-syntax-of-your-android-code/  
   https://github.com/vincentbrison/vb-android-app-quality/blob/master/config/quality.gradle
