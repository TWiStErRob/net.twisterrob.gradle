# Gradle test plugin

Helps integration testing of Gradle plugins and tasks.

## Usage

```groovy
plugins {
	id("net.twisterrob.gradle.plugin.gradle.test")
}

dependencies {
	// Test framework (`GradleRunnerRule` is written for JUnit)
	testImplementation "junit:junit:${VERSION_JUNIT}"
	// Not necessary, but useful in IntelliJ IDEA (see `@Language`)
	testImplementation "org.jetbrains:annotations:${VERSION_JETBRAINS_ANNOTATIONS}"
}
```

or more explicitly:

```groovy
plugins {
	id("java-gradle-plugin")
}

dependencies {
	// assuming the plugin being tested is in this module
	implementation gradleApi()

	testImplementation gradleTestKit()
	testImplementation 'net.twisterrob.gradle:twister-gradle-test:+' // replace version as needed
	// Test framework (`GradleRunnerRule` is written for JUnit)
	testImplementation "junit:junit:${VERSION_JUNIT}"
	// Not necessary, but useful in IntelliJ IDEA (see `@Language`)
	testImplementation "org.jetbrains:annotations:${VERSION_JETBRAINS_ANNOTATIONS}"
}
```

### Basic structure in JUnit (Groovy)

```groovy
class MyTest {
	@Rule public final GradleRunnerRule gradle = new GradleRunnerRule()
	
	@Test void "gradle test"() {
		given:
		@Language('gradle')
		def script = """\
		task test {
			doLast {
			    println 'Hello World'
			}
		}
		""".stripIndent()
	
		when:
		def result = gradle.run(script, 'test').build()
	
		then:
		assert result.task(':test').outcome == TaskOutcome.SUCCESS
		result.assertHasOutputLine(/Hello World/)
	}
}
```

See `net.twisterrob.gradle.test.GradleRunnerRuleTest` for more examples.

### Running Gradle Test Kit tests
To run tests from **Android Studio 3.1**, run it as usual, but edit the "**Gradle-aware Make**" to run `classes testClasses` tasks or `:module:classes :module:testClasses`.
If this doesn't work, try to `gradlew build` the whole project and then run it again.

To run this test from **IntelliJ IDEA**, run it as usual, but first set: *Build, Execution, Deployment > Build Tools > Gradle > Runner > Run tests using:* in Settings to "**Gradle Test Runner**"

#### Potential test failure reasons:
 * `ANDROID_HOME` is missing from the system:  
   `export ANDROID_HOME=.../android/sdk`
 * `build/pluginUnderTestMetadata/plugin-under-test-metadata.properties` is missing  
   run `./gradlew test` from the command line once to generate the files
 * `Could not get unknown property 'net' for root project 'junit1493727323926151858' of type org.gradle.api.Project.`  
   This happens because the injected classpath is exposed, but not applied to the build script.
   Apply a named plugin with its ID to get access to its classes.

### Debugging

#### Gradle Test Kit's `.withDebug(true)`
By default `gradleTestKit()` runs in a separate daemon process, so it's not possible to attach to it. To change this `withDebug` was provided, which will make the test build run in embedded mode.
```groovy
gradle.run(/*...*/).withDebug(true).build()
```
Running a test in embedded mode allows us to put breakpoints inside the tasks and plugins that are in this project. Without it only the test classes and their utilities (i.e. test project setup) would be available without visibility to the internals of the build the test is running.

**Beware** (`ClassNotFoundException: groovy.util.AntBuilder`): https://github.com/gradle/gradle/issues/3995

#### Debug external Gradle execution
 * Terminal: run as `gradlew --no-daemon -Dorg.gradle.debug=true <tasks...>`
 * IDEA: attach Remote debugger (from Run configurations) with socket on `localhost:5005`

### Recipes

#### Dump applied `plugins`

```groovy
gradle.buildFile << """\
	allprojects {
		project.plugins.whenPluginAdded { Plugin plugin ->
			println "${project.path} (${plugin.class.name}): ${plugin}"
		}
	}
""".stripIndent()
```

#### Ignore test for specific Gradle version
This is discouraged, but sometimes necessary (e.g. the feature on the older Gradle version would be hard to implement).

```kotlin
assumeThat(
   "Feature X was added in Gradle 7.2.",
   gradle.gradleVersion.baseVersion,
   greaterThanOrEqualTo(GradleVersion.version("7.2"))
)
```
where `gradle` is a `GradleRunnerRule` instance, and `gradle.gradleVersion` is pretty much equivalent to:
```kotlin
val gradleVersion: String by systemProperty("net.twisterrob.gradle.runner.gradleVersion")
```
