# Change Log


## 0.14 *(2021-12-20 --- )*

### Breaking
* ...

### New
* Gradle 7 compatible (up to 7.4) (#207)

### Fixes
* ...

### Deprecations

#### `net.twisterrob.gradle:artifact-name`
* ...

### Internal
* ...


## 0.13 *(2021-11-01 --- 2021-12-20)*

### Breaking
 * convention: Removed `autoVersion` DSL, it's automatically deciding whether it needs auto-versioning. (#174)
 * convention: Removed `addRunTasks` DSL, using lazy task creations instead. (#189)
 * Some internal moves and renames of classes. (#177, #184, #195/#197)
 * Lazy/up-to-date improvements (#11/#186/#189, #194/#196)

### New
 * Gradle 7 compatible (up to 7.3) (#163)
 * quality: Android Gradle Plugin 7.0.3 compatible (#180)  
   (still uses some deprecated code)
 * New compatibility modules for compile-time checks for old code. (#181)
 * quality: `validateViolations` and `violationCountFile` tasks (#195/#197)

### Fixes
 * convention: `-dump` option is not supported by R8 (#175/#179)
 * Kotlin, Java, resources are published in -sources.jar (#187/#192)
 * convention: Fail fast on release (#188/#191)

### Internal
 * Test improvements (#162, #176, #193)
 * Build improvements (#166, #167, #146/#168, #171, #173, #178, #182)
 * convention: plugin is split into multiple pieces. (#173)
 * Code cleanup (#170, #184, #11/#186/#189)


## 0.12 *(2021-06-06 --- 2021-11-01)*

### New
 * quality: Android Gradle Plugin 4.2.1 compatible (#126)
 * quality: Android Gradle Plugin 4.2.2 compatible (#145)
 * New artifact: `twister-convention-plugins`  
   Private plugins integrated (#95, #149, #142, #148), 
   for history see [CHANGELOG](plugin/CHANGELOG.md).
 * convention: use latest versions of things  
   AGP 4.2.2 / Build Tools 30.0.2 (#143) / Gradle 6.9.1 (#141) / Kotlin 1.4.32 / JUnit 5 (#133)
 * convention: R8 support (#144)
 * reports: auto-expand all code blocks (#127)
 * reports: Custom output for LintError (#27/#123)
 * reports: Add back to top button (#59/#152)
 * reports: Render lint title as markdown (#130/#153)

### Fixes
 * quality: Fix `&amp;` escaping in lint (#65/#129)
 * quality: Mention violationReport* tasks from :lint output (#24/#125)
 * convention: migrate release task configuration to be lazy (#104/#134)

### Internal
 * tests: lintOptions.check syntax (#124/#128)
 * convention: Move from GrGit to JGit direct usage (#131/#132)
 * Improve GitHub actions: (#135, #136, #150)


## 0.11 *(2020-11-22 --- 2021-06-06)*

### Breaking
 * Removed Bintray publishing as it shut down, replaced with Maven Central. Use `mavenCentral()` instead of custom Bintray url. (#107/#108)
 * Build with Kotlin 1.4.32 (#112), this also comes as transitive dependency.

### New
 * Android Gradle Plugin 4.0.2 compatible (#101)
 * Android Gradle Plugin 4.1.3 compatible (#101)
 * Android Gradle Plugin 4.2.0 compatible (#101)
 * Full project examples as documentation (#116), see [docs/examples](docs/examples).

### Fixes
 * Use new Checkstyle API in Gradle 6.9 (#114) to fix:
   > The Checkstyle.setConfigDir() method has been deprecated. This is scheduled to be removed in Gradle 7.0. Please use the Checkstyle.getConfigDirectory().set() method instead.

 * Use conventions instead of manually created properties (#102/#117) to fix:
   > Querying the mapped value of task ':violationReportHtml' property 'xml' before task ':violationReportHtml' has completed has been deprecated. This will fail with an error in Gradle 7.0.

### Deprecations

#### `net.twisterrob.gradle:twister-gradle-test`
* `GradleRunnerRule.templateFile(String): File`
* `GradleRunnerRule.templateFile(String, Any?): File`
* `GradleRunnerRule.setGradleVersion(String)`

### Internal
 * Moved Continuous Integration from Travis to GitHub Actions (#110).
 * Use Gradle 6.9 to build (#114).
 * Enable Parallel builds (#119/#120)
 * Bump test library versions to latest (#115).
 * Allow ignored tests to run on CI (#72/#110).
 * Let Gradle decide PMD version and adapt to it (#81/#118).
 * Share code style and inspections (#109).
 * Clarify ANDROID_HOME variable (#75/#121).


## 0.10 *(2020-01-12 --- 2020-11-22)*

### Breaking
 * `ValidateViolationsTask.action` is removed, override `processViolations` instead (#85).

### New
 * Gradle 5 compatible (up to 5.6.4) (#84)
 * Use Gradle 5.6.4 to build (#84)
 * Use Kotlin DSL 5.6.4 (#84)
 * Android Gradle Plugin 3.5.3 compatible (Kotlin 1.3.50) (#86)
 * Android Gradle Plugin 3.6.4 compatible (Kotlin 1.3.72) (#98)

### Fixes
 * Make sure nothing is logged when lint task is disabled. (#82/#83)
 * Reduce empty output
 * Output generated files from violationReportHtml

### Internal
 * Gradle validateTaskProperties issues (#85).
 * Test against latest minor versions of AGP (#96)

## 0.9 *(2019-02-19 --- 2019-07-11)*

### New
 * Gradle 5 compatible (up to 5.4.1) (#78)
 * Android Gradle Plugin 3.3.2 compatible (Kotlin 1.3.21) (#78)
 * Android Gradle Plugin 3.4.2 compatible (Kotlin 1.3.31) (#79)

### Fixes
 * Clean up suppressions, categories and names for CheckStyle and PMD (#60, #63, #64 / #73)
 * Don't show `details>summary` when there's no description (#61/#74)
 * Add polyfill for 5.0-removed behavior (https://github.com/gradle/gradle/issues/6263),  
   for more info and origin see https://github.com/gradle/gradle/issues/2765 (#78)

### Internal
 * Migrate all tests to JUnit 5 (Jupiter) (#68)
 * Android Studio only works with settings.gradle.kts (#71)
 * Improve documentation (#69)
 * Move to standard Java XML generation to reduce memory usage (#62/#66)
 * Fix breaking change: https://github.com/checkstyle/checkstyle/issues/6478
 * Use Gradle 4.10.3 to build (#76/#77)
 * Use Gradle 5.4.1 to build (#78)


## 0.8 *(2018-10-14 --- 2019-02-18)*

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
