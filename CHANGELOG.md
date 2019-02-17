# Change Log

## 0.8 *(2018-10-14 --- 2019-02-??)*

### Breaking
 * `GradleRunnerRule.buildFile`/`settingsFile` are now properties, not functions

### New
 * Android Gradle Plugin 3.2.1 (#34)
 * Android Gradle Plugin 3.3 and 3.1 compatible (#55, #57)
 * `net.twisterrob.gradle.runner.gradleVersion` property to allow running on different version with ease (#57)
 * `net.twisterrob.gradle.runner.clearAfterSuccess` property to allow keeping files after runs (#46)

### Fixes
 * Better error handling and display (#55, #52/#54, #31/#50, #29/#38, #32/#36)
 * Remove duplication from HTML report with multiple variants (#51/#53)
 * Better up-to-date handling and Gradle lifecycle handling (#25/#43, #35/#37, #58)
 * Automatic linking of URLs in lint markdown descriptions (#39)

### Internal
 * Other minor version bumps (#34)
 * Lot of internal cleanup and tests (#45, #47, #10/#22/#42, #40/#41)
 * Better travis JUnit output: folded stdout/exception blocks (#44)

## 0.7 *(2018-10-01 --- 2018-10-10)*
 * Publish `-sources.jar` files (#13)
 * Android Gradle Plugin 3.2.0 (#12)
 * Use Android 28 for testing (#12)
 * Gradle: 4.9 (AGP 3.2.0 needs 4.6+) (#12)
   * 4.10.2 is GA, but has many breaking changes (e.g. kotlin-dsl and lazy task configuration)
 * New model for Violations, a grouped property approach (#14) 
 * New task for HTML report with limited Lint support (#14)
 * Tasks are now automatically added when applying `quality` plugin (#14, #18, #20, #21)
 * Fixed empty data handling in TableGenerator (#16)

## 0.6 *(2018-08-17 --- 2018-10-01)*
 * Gradle: 4.5.1
 * Android Gradle Plugin 3.1.4
 * Kotlin 1.2.71 w/ DSL 1.0.RC11

## 0.5 *(2018-04-02 --- 2018-04-16)*
 * Gradle: 4.4.1
 * Android Gradle Plugin 3.1.1
 * more test coverage

## 0.4 *(2018-01-28 --- 2018-04-02)*
 * Minor fixes and more tests
 * [Checkstyle] Fix `config_dir` setup
 * [PMD] Use Android res folder
 * [PMD] Add configuration folder to classpath for `<rule ref=`
 * Experimental exclusion support
 * Travis CI
 * JCenter publishing via Bintray
 * Version bumps

## 0.3-SNAPSHOT *(2018-01-20 --- 2018-01-28)*
_Never published to a public repository._

 * Komplete Konversion: Converted most of the plugin to Kotlin.  
   *Only a single Groovy test remains which tests an interface is nicely consumable in Groovy.*
 * Merged PMD and Checkstyle classes to remove duplication
 * Support XML (parsed) and HTML (for people) reports side-by-side


## 0.2-SNAPSHOT *(2018-01-16 --- 2018-01-20)*
_Never published to a public repository._

 * Change artifact names to include `twister-` prefix.
 * Violation count withing Grouping
 * Summary table for violations
 * Global lint to rule them all


## 0.1-SNAPSHOT *(2018-01-04 --- 2018-01-15)*
_Never published to a public repository._

 * Gradle: 4.2.1
 * Android Gradle Plugin 3.0.1
 * Preliminary Checkstyle support
 * Preliminary PMD support
 * some level of multi-module test support
