package net.twisterrob.gradle.test

import net.twisterrob.gradle.BaseIntgTest
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals

/**
 * @see GradleRunnerRule
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class GradleRunnerRuleTest_usage : BaseIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Test fun `gradle script test`() {
		@Language("gradle")
		val script = """
			println 'Hello World'
		""".trimIndent()

		val result = gradle.runBuild {
			run(script)
		}

		result.assertHasOutputLine("Hello World")
	}

	@Test fun `gradle task test`() {
		@Language("gradle")
		val script = """
			task test {
				doLast {
				    println 'Hello World'
				}
			}
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "test")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":test")!!.outcome)
		result.assertHasOutputLine("Hello World")
	}

	@Test fun `test with file`() {
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

		val result = gradle.runBuild {
			run(script, "printConfigFile")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":printConfigFile")!!.outcome)
		assertThat(result.output, containsString(configFileContents))
	}

	@Test fun `buildFile from multiple basedOn merged into one including script`(@TempDir temp: File) {
		fun generateFolder(name: String): File {
			val folder = temp.resolve("base_$name").apply { mkdirs() }
			folder
				.resolve("build.gradle")
				.writeText("""println("basedOn($name)")${System.lineSeparator()}""")
			return folder
		}

		(1..3).forEach { gradle.basedOn(generateFolder(it.toString())) }

		val result = gradle.runBuild {
			run("""println("script()")""", ":help")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":help")!!.outcome)
		result.assertHasOutputLine("""basedOn(1)""")
		result.assertHasOutputLine("""basedOn(2)""")
		result.assertHasOutputLine("""basedOn(3)""")
		result.assertHasOutputLine("""script()""")
	}
}
