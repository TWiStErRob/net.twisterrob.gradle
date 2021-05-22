package net.twisterrob.gradle.test

import junit.runner.Version
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(GradleRunnerRuleExtension::class)
class TestPluginTest {

	private lateinit var gradle: GradleRunnerRule

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
	@Test fun `gradle test plugin test`() {
		val triplet = "\"\"\""
		@Language("groovy")
		val testFileContents = """
			//noinspection GrPackage it will be written to the right folder
			package net.twisterrob.gradle.test

			import org.junit.Rule
			import org.junit.Test

			class Testception {

				@Rule public final GradleRunnerRule gradle = new GradleRunnerRule()

				@Test void "gradle script test"() {
					given:
					//@Language("gradle")
					def script = ${triplet}\
						println 'Hello World'
					${triplet}.stripIndent()

					when:
					def result = gradle.run(script).build()

					then:
					result.assertHasOutputLine(/Hello World/)
				}
			}
		""".trimIndent()
		gradle.file(testFileContents, "src/test/groovy/net/twisterrob/gradle/test/Testception.groovy")

		val artifactPath = System.getProperties()["net.twisterrob.gradle.test.artifactPath"].toString()
		@Language("gradle")
		val script = """
			apply plugin: 'groovy'
			apply plugin: 'net.twisterrob.gradle.test'

			repositories {
				ivy {
					// make /test/build/libs/X-0.0.jar available as 'net.twisterrob.gradle:X:0.0'
					url '${artifactPath.replace("\\", "\\\\")}'
					// Using layout(String, Closure) instead of patternLayout(Action) for backwards compatibility with Gradle 4.x
					layout('pattern') {
						artifact '[artifact]-[revision].[ext]'
						m2compatible = true
					}
					// https://docs.gradle.org/nightly/userguide/upgrading_version_5.html#maven_or_ivy_repositories_are_no_longer_queried_for_artifacts_without_metadata_by_default
					metadataSources {
						artifact()
					}
				}
				mavenCentral()
			}
			dependencies {
				testImplementation 'org.jetbrains.kotlin:kotlin-stdlib:${KotlinVersion.CURRENT}'
				testImplementation 'junit:junit:${Version.id()}'
			}
			// output test execution result so we can verify it actually ran
			test.afterTest { desc, result ->
				logger.quiet "${'$'}{desc.className} > ${'$'}{desc.name}: ${'$'}{result.resultType}"
			}
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "test")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":test")!!.outcome)
		result.assertHasOutputLine("net.twisterrob.gradle.test.Testception > gradle script test: SUCCESS")
	}
}
