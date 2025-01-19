package net.twisterrob.gradle.test

import net.twisterrob.gradle.BaseIntgTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * @see GradleRunnerRule
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class GradleRunnerRuleTest_usage : BaseIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Test fun `gradle script test`() {
		@Language("gradle")
		val script = """
			println("Hello World")
		""".trimIndent()

		val result = gradle.runBuild {
			run(script)
		}

		result.assertHasOutputLine("Hello World")
	}

	@Test fun `gradle task test`() {
		@Language("gradle")
		val script = """
			tasks.register("test") {
				doLast {
					println("Hello World")
				}
			}
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "test")
		}

		result.assertSuccess(":test")
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
			tasks.register("printConfigFile") {
				def configFile = rootProject.file('config/file.xml')
				inputs.file(configFile)
				doLast {
					println(configFile.text)
				}
			}
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "printConfigFile")
		}

		result.assertSuccess(":printConfigFile")
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

		result.assertSuccess(":help")
		result.assertHasOutputLine("""basedOn(1)""")
		result.assertHasOutputLine("""basedOn(2)""")
		result.assertHasOutputLine("""basedOn(3)""")
		result.assertHasOutputLine("""script()""")
	}
}
