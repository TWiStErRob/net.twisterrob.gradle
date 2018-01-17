# Twister's Gradle Quality plugins
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
task('tests', type: net.twisterrob.gradle.quality.tasks.GlobalTestTask)
```

## Development

### Structure

| Module \\<br>Location | Distributed as Artifact \\<br>Java/Groovy Package | Summary |
| --- | --- | --- |
| quality<br>[`/quality`](quality) |`'net.twisterrob.gradle:twister‑quality'`<br>`net.twisterrob.gradle.quality` | All quality plugins bundled in one. |
| common<br>`/common` | `'net.twisterrob.gradle:twister‑quality‑common'`<br>`net.twisterrob.gradle.common` | Shared classes between checkers.<br>_Not to be consumed directly._ |
| checkstyle<br>`/checkers/checkstyle` | `'net.twisterrob.gradle:twister-quality-checkstyle'`<br>`net.twisterrob.gradle.checkstyle` | Checkstyle setup plugin for Gradle. |
| pmd<br>`/checkers/pmd` | `'net.twisterrob.gradle:twister-quality-pmd'`<br>`net.twisterrob.gradle.pmd` | PMD setup plugin for Gradle. |
| test<br>`/test` | `'net.twisterrob.gradle:twister-gradle-test'`<br>`net.twisterrob.gradle.test` | Gradle test plugin and resources. |

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
