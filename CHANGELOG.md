# Change Log

## 0.7 *(2018-10-01 --- )*
 * Android Gradle Plugin 3.2.0
 * Use Android 28 for testing
 * Gradle: 4.9 (AGP 3.2.0 needs 4.6+)
   * 4.7 `val prop by project` -> `val prop: String by project`  
     (4.6 doesn't work with `val prop: String by project`)
   * 4.7 made `Checkstyle.getConfigProperties` nullable
   * 4.7 made `Pmd.getClasspath` nullable
   * 4.7 deprecation warning:
     > The following annotation processors were detected on the compile classpath: 'com.google.auto.value....'.
   * 4.8 "Please use kotlin-stdlib-jdk7 instead", probably because of newer bundled Kotlin
   * 4.8 deprecation warning:
     > As part of making the publishing plugins stable, the 'deferred configurable' behavior of
    the 'publishing {}' block is now deprecated
    * 4.10.2 is GA, but breaks gradle.kts

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
