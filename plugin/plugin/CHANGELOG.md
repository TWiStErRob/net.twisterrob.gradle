## 4.0.x

### 4.0.2.14-29-29.2

### 4.0.2.14-29-29.1
 - Expose AGP as api dependency -> maven.pom scope=compile

### 4.0.2.14-29-29.0
 - AGP 4.0.2
   - Remove net.twisterrob.android-feature as com.android.feature was deprecated
   - `android.enableR8=false` gives a deprecation warning, but R8 is not supported yet.
 - Gradle 6.1.1  
   Note: use `apply from: resources.text.fromInsecureUri("http://localhost/maven/configure.gradle")`
 - Target Java 8 for class files
 - Bump internal libraries to latest

## 3.6.x

### 3.6.4.14-29-29.0
 - AGP 3.6.4
 - Kotlin 1.3.72 / DSL 5.6.4

## 3.5.x

### 3.5.4.14-29-29.0
 - AGP 3.5.4

## 3.4.x
 
### 3.4.2.14-29-29.1
 - Add `net.twisterrob.java-library` plugin
 - Automatically detect `-Xlint:opt -Xlint:-opt` and disable wins.
 - Exclude classpath leaked annotation processors

### 3.4.2.14-29-29.0
 - Gradle 5.6.4
 - Default to API 29 / Android 10 (Q) (target and compile SDK)
 - Build Tools 29.0.2

### 3.4.2.14-28-28.0
 - AGP 3.4.2  
   Disabled R8 in tests, using Proguard still
 - Gradle 5.1.1
 - Kotlin 1.3.31 / DSL 1.1.3

## 3.3.x

### 3.3.2.14-28-28.2
 - Revert Kotlin to compatible 1.3.21 version

### 3.3.2.14-28-28.1
 - Allow to be applied on Gradle 5+
 - Use Java 8 as target version to allow for desugaring (e.g. default interface methods)
 - Use Robolectric 4.3.1 for testing
 - Use JUnit 4.13 and newest Hamcrest for testing
 - Use latest dex-member-list
 
### 3.3.2.14-28-28.0
 - AGP 3.3.2
 - Gradle 4.10.3 (Gradle 4.10.1+)
 - Kotlin 1.3.21 / DSL RC11
 - `android.defaultConfig.version.versionFile` removed  
   `version.properties` is used to read defaults

## 3.2.x

### 3.2.1.14-28-28.2
 - Exclude readmes from the final APK

### 3.2.1.14-28-28.1
 - Make tests run on Java 9-11
 - Use Robolectric 4.1 for testing
 - Support com.android.test modules for Kotlin
 - Gradle 4.10.2 (Gradle 4.6+)

### 3.2.1.14-28-28.0
 - AGP 3.2.1
 - Gradle 4.10.2 (Gradle 4.6+)

## 3.1.x

### 3.1.4.14-28-28.2
 - Fix crash in ProGuard file extractions
 - Update ProGuard rules to hide some new framework Notes
 - Exclude some unused files from the final APK

### 3.1.4.14-28-28.1
 - No more Groovy in plugin code!

### 3.1.4.14-28-28.0
 - Default to Android 28 9.0 Pie

### 3.1.4.14-27-27.4
 - Fix TestReportGenerator task
 - Re-enable compile task metadata fixes
 - minor fixes and internal refactors

### 3.1.4.14-27-27.3
 - Clean up old class usage after konversion (XmlSlurper, Date)
 - General clean up
 - UNSTABLE: TestReportGenerator task is not usable

### 3.1.4.14-27-27.2
 - Fixed SVN VCS integration
 - Konvert VCS, Java, Release, Build, Utils plugins
 - Only one Groovy method remains
 - Lots of new tests
 - Kotlin 1.2.71 / DSL RC11
 - UNSTABLE: TestReportGenerator task is not usable

### 3.1.4.14-27-27.1
 - Fixed compatibility with `net.twisterrob.gradle`
 - UNSTABLE: SVN verisoning doesn't work

### 3.1.4.14-27-27.0
 - AGP 3.1.4
 - Kotlin 1.2.60
 - Decreased BuildConfig.BUILD_TIME frequency
 - ProGuard convetion support for Android library modules
 - UNSTABLE: cannot be used together with [`net.twisterrob.gradle:twister-gradle-common`](https://github.com/TWiStErRob/net.twisterrob.gradle)

### 3.1.3.14-27-27.0
 - Gradle 4.3, 4.4, 4.4.1, 4.5, 4.5.1  
   works with 4.6+, but warns  
   breaks with configure on demand enabled

## 3.0.x

### 3.0.1.10-19-26.0
 - Gradle 4.1, 4.2  
   Gradle 4.3 deprecated bootClasspath


## 2.3.x

## 2.3.3.10-19-26.1 (0.9.1)
 - Gradle 3.5.1
