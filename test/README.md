Using this artifact:
```groovy
apply plugin: 'java-gradle-plugin'

dependencies {
	// assuming the plugin is in this module
	implementation localGroovy()
	implementation gradleApi()

	testImplementation gradleTestKit()
	testImplementation 'net.twisterrob.gradle:test:+'
	// Test framework (`GradleRunnerRule` is written for JUnit)
	testImplementation "junit:junit:${VERSION_JUNIT}"
	// Not necessary, but useful in IntelliJ IDEA (see `@Language`)
	testImplementation "org.jetbrains:annotations:${VERSION_JETBRAINS_ANNOTATIONS}"
}
```

Basic structure:
```groovy
class MyTest {
	@Rule public final GradleRunnerRule gradle = new GradleRunnerRule()
	
	@Test void "gradle test"() {
		given:
		@Language('gradle')
		def script = """
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
		assert result.output =~ /(?m)^Hello World$/
	}
}
```
See `src/test/groovy/net/twisterrob/gradle/test/GradleRunnerRuleTest` for more examples.
