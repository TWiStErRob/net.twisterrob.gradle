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


### Debugging

#### `withDebug`
By default `gradleTestKit()` runs in a separate daemon process, so it's not possible to attach to it. To change this `withDebug` was provided, which will make the test build run in embedded mode.
```groovy
def result = gradle.run(/*...*/).withDebug(true).build()
```
Running a test in embedded mode allows us to put breakpoints inside the tasks and plugins that are in this project. Without it only the test classes and their utilities (i.e. test project setup) would be available without visibility to the internals of the build the test is running.

#### `Plugins`
```groovy
gradle.buildFile << """\
	allprojects {
		project.plugins.whenPluginAdded { Plugin plugin ->
			println "${project.path} (${plugin.class.name}): ${plugin}"
		}
	}
"""
```

## Useful articles
 * https://proandroiddev.com/configuring-android-project-static-code-analysis-tools-b6dd83282921
 * https://medium.com/mindorks/static-code-analysis-for-android-using-findbugs-pmd-and-checkstyle-3a2861834c6a
 * http://vincentbrison.com/2014/07/19/how-to-improve-quality-and-syntax-of-your-android-code/  
   https://github.com/vincentbrison/vb-android-app-quality/blob/master/config/quality.gradle
