package net.twisterrob.gradle.test

import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.Test

class TestPluginTest {

	@Rule public final GradleRunnerRule gradle = new GradleRunnerRule()

	/**
	 * Set up a full Gradle project in a test that has a test to test the plugin that helps testing Gradle.
	 *
	 * Let's break that down:<ul>
	 * <li>This test sets up a Gradle project with:<ul>
	 *     <li>{@code apply plugin: 'net.twisterrob.gradle.test'}
	 *     <li>{@code Testception.groovy}
	 *     </ul>
	 * <li>{@code Testception} sets up a simple Gradle build and checks its output.
	 * <li>{@code Testception} is being run from {@code :test} task in the project that's set up in this test method.
	 */
	@Test void "gradle test plugin test"() {
		given:
		//noinspection GrPackage it will be written to the right folder
		@Language('groovy')
		def testFileContents = '''\
			package net.twisterrob.gradle.test

			import org.junit.Rule
			import org.junit.Test

			class Testception {

				@Rule public final GradleRunnerRule gradle = new GradleRunnerRule()

				@Test void "gradle script test"() {
					given:
					def script = """\\
						println 'Hello World'
					""".stripIndent()

					when:
					def result = gradle.run(script).build()

					then:
					result.assertHasOutputLine(/Hello World/)
				}
			}
		'''.stripIndent()
		gradle.file(testFileContents,
				'src/test/groovy/net/twisterrob/gradle/test/Testception.groovy')

		def artifactPath = System.properties['net.twisterrob.gradle.test.artifactPath'].toString()
		@Language('gradle')
		def script = """\
			apply plugin: 'groovy'
			apply plugin: 'net.twisterrob.gradle.test'

			repositories {
				ivy {
					// make /test/build/libs/test-1.0.jar available as 'net.twisterrob.gradle:test:1.0'
					url '${artifactPath.replace('\\', '\\\\')}'
					layout('pattern') {
						artifact '[artifact]-[revision].[ext]'
						m2compatible = true
					}
				}
				mavenCentral()
			}
			dependencies {
				testImplementation 'junit:junit:4.12'
			}
			// output test execution result so we can verify it actually ran
			test.afterTest { desc, result -> logger.quiet "\${desc.className} > \${desc.name}: \${result.resultType}" }
		""".stripIndent()

		when:
		def result = gradle.run(script, 'test').build()

		then:
		assert result.task(':test').outcome == TaskOutcome.SUCCESS
		result.assertHasOutputLine(/net.twisterrob.gradle.test.Testception > gradle script test: SUCCESS/)
	}
}
