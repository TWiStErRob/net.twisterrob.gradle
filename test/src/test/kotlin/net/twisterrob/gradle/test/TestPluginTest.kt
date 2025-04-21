package net.twisterrob.gradle.test

import junit.runner.Version
import net.twisterrob.gradle.BaseIntgTest
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @see TestPlugin
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class TestPluginTest : BaseIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	/**
	 * Set up a full Gradle project in a test that has a test to test the plugin that helps to test Gradle.
	 *
	 * Let's break that down:
	 *  * This test sets up a Gradle project with:
	 *    * `plugins { id("net.twisterrob.gradle.plugin.gradle.test") }`
	 *    * `Testception.groovy`
	 *  * `Testception` sets up a simple Gradle build and checks its output.
	 *  * `Testception` is being run from `:test` task in the project that's set up in this test method.
	 */
	@Suppress("detekt.LongMethod") // Multiple files are listed in this one method.
	@Test fun `gradle test plugin test`() {
		@Suppress("detekt.StringShouldBeRawString") // https://github.com/detekt/detekt/issues/6145#issuecomment-1563091070
		val triplet = "\"\"\""
		@Suppress("GrPackage") // It will be written to the right folder.
		@Language("groovy")
		val testFileContents = """
			package net.twisterrob.gradle.test.test
			
			import net.twisterrob.gradle.test.GradleRunnerRule
			import org.junit.Rule
			import org.junit.Test
			
			class Testception {
			
				@Rule public final GradleRunnerRule gradle = new GradleRunnerRule()
			
				@Test void "gradle script test"() {
					given:
					//@Language("gradle")
					def script = ${triplet}\
						println("Hello World")
					${triplet}.stripIndent()
			
					when:
					def result = gradle.run(script).build()
			
					then:
					result.assertHasOutputLine(/Hello World/)
				}
			}
		""".trimIndent()
		gradle.file(testFileContents, "src/test/groovy/net/twisterrob/gradle/test/test/Testception.groovy")

		val artifactPath = System.getProperty("net.twisterrob.gradle.test.artifactPath")
			?: error("Missing property: net.twisterrob.gradle.test.artifactPath")
		@Language("gradle")
		val script = """
			plugins {
				id("org.gradle.groovy")
				id("net.twisterrob.gradle.plugin.gradle.test")
			}
			
			repositories {
				ivy {
					// make /test/build/libs/X-0.0.jar available as 'net.twisterrob.gradle:X:0.0'
					url = "${artifactPath.replace("\\", "\\\\")}"
					// patternLayout(Action) was introduced in 5.0, layout(String, Closure) was removed in 7.0.
					patternLayout {
						artifact("[artifact]-[revision].[ext]")
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
				testImplementation("junit:junit:${Version.id()}")
			}
			// Output test execution result, so we can verify it actually ran.
			// This doesn't work with configuration cache: https://github.com/gradle/gradle/issues/25311
			test.afterTest { desc, result ->
				logger.quiet("${'$'}{desc.className} > ${'$'}{desc.name}: ${'$'}{result.resultType}")
			}
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "test")
		}

		result.assertSuccess(":test")
		result.assertHasOutputLine("net.twisterrob.gradle.test.test.Testception > gradle script test: SUCCESS")
	}
}
