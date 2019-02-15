package net.twisterrob.gradle.test

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class GradleRunnerRuleTest {

	@Rule @JvmField val gradle = GradleRunnerRule()

	@Test fun `gradle script test`() {
		`given`@
		@Language("gradle")
		val script = """
			println 'Hello World'
		""".trimIndent()

		val result: BuildResult
		`when`@
		result = gradle.run(script).build()

		`then`@
		result.assertHasOutputLine("Hello World")
	}

	@Test fun `gradle task test`() {
		`given`@
		@Language("gradle")
		val script = """
			task test {
				doLast {
				    println 'Hello World'
				}
			}
		""".trimIndent()

		val result: BuildResult
		`when`@
		result = gradle.run(script, "test").build()

		`then`@
		assertEquals(TaskOutcome.SUCCESS, result.task(":test")!!.outcome)
		result.assertHasOutputLine("Hello World")
	}

	@Test fun `test with file`() {
		`given`@
		@Language("xml")
		val configFileContents = """
			<root>
				<element attr="value" />
			</root>
		""".trimIndent()
		gradle.file(configFileContents, "config/file.xml")

		@Language("gradle")
		val script = """
			task printConfigFile {
				def configFile = rootProject.file('config/file.xml')
				inputs.file configFile
				doLast {
					println configFile.text
				}
			}
		""".trimIndent()

		val result: BuildResult
		`when`@
		result = gradle.run(script, "printConfigFile").build()

		`then`@
		assertEquals(TaskOutcome.SUCCESS, result.task(":printConfigFile")!!.outcome)
		assertThat(result.output, containsString(configFileContents))
	}

	@Test fun `buildFile from multiple basedOn merged into one`() {
		`given`@
		@Language("gradle")
		val script = """
			task test {
				doLast {
				    println 'Hello World'
				}
			}
		""".trimIndent()

		val result: BuildResult
		`when`@
		result = gradle
			.basedOn("")
			.basedOn("")
			.basedOn("")
			.run(script, "test").build()

		`then`@
		assertEquals(TaskOutcome.SUCCESS, result.task(":test")!!.outcome)
		result.assertHasOutputLine("Hello World")
	}
}
