## 3.3.x

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
