# Gradle Quality plugins

```gradle
apply plugin: 'net.twisterrob:quality'
```
above includes (but you can cherry-pick them):
```gradle
apply plugin: 'net.twisterrob:checkstyle'
```


## Versions

### 0.1: 2018-01-xx
Gradle: 4.2.1, Android Gradle Plugin 3.0.1


## Development

### Project

1. Make sure it runs successfully from terminal with `./gradlew test`  
   Check failure reason (JUnit report) for things to fix
2. After this it's ok to import the root `build.gradle` into IntelliJ IDEA/Android Studio

### Tests
To run tests from Android Studio, run it as usual, but edit the "Gradle-aware Make" to run `classes testClasses` tasks or `:module:classes :module:testClasses`.
If this doesn't work, try to `gradlew build` the whole project and then run it again from AS.

To run this test from IntelliJ IDEA, run it as usual, but first set: *Build, Execution, Deployment > Build Tools > Gradle > Runner > Run tests using:* in Settings to **Gradle Test Runner**

#### Potential test failure reasons:
 * `ANDROID_HOME` is missing from the system:  
   `export ANDROID_HOME=.../android/sdk`
 * `build/pluginUnderTestMetadata/plugin-under-test-metadata.properties` is missing  
   run `./gradlew test` from the command line once to generate the files
