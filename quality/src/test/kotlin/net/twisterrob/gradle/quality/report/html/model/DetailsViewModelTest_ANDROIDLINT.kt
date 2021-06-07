package net.twisterrob.gradle.quality.report.html.model

import com.flextrade.jfixture.JFixture
import net.twisterrob.gradle.quality.Violation
import org.gradle.api.Project
import org.gradle.api.Task
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.io.File
import kotlin.test.assertEquals

class DetailsViewModelTest_ANDROIDLINT {

	private val fixture = createAndroidLintFixture()

	@Test
	fun `message without escapes goes through as is`() {
		val model = DetailsViewModel(fixture.build<Violation>().apply {
			val lintMessage = """
				Title
				just a message
			""".trimIndent()
			setField("message", lintMessage)
		})

		val result = model.messaging.message

		assertEquals("""just a message""", result)
	}

	@Test
	fun `message with escapes gets escaped`() {
		val model = DetailsViewModel(fixture.build<Violation>().apply {
			// make sure message goes through the transformation
			setField("rule", "IconMissingDensityFolder")
			val lintMessage = """
				Title
				something with escapes:\n 1:\ 2:\\ 3:\\\ 4:\\\\
			""".trimIndent()
			setField("message", lintMessage)
		})

		val result = model.messaging.message

		assertEquals("""something with escapes:\\n 1:\\ 2:\\\\ 3:\\\\\\ 4:\\\\\\\\""", result)
	}

	@Test
	fun `IconMissingDensityFolder specific message escapes are removed`() {
		val model = DetailsViewModel(fixture.build<Violation>().apply {
			setField("rule", "IconMissingDensityFolder")
			val lintMessage = """
				Title
				Missing density variation folders in `src\\main\\res`: drawable-hdpi
			""".trimIndent()
			setField("message", lintMessage)
		})

		val result = model.messaging.message

		assertEquals("""Missing density variation folders in \`src\\main\\res\`: drawable-hdpi""", result)
	}

	@Test
	fun `LintError specific message gets formatted`() {
		val model = DetailsViewModel(fixture.build<Violation>().apply {
			setField("rule", "LintError")
			val summary = "Lint Failure"
			val message =
				"Unexpected failure during lint analysis of module-info.class (this is a bug in lint or one of the libraries it depends on)&#xA;&#xA;Stack: `NullPointerException:InvalidPackageDetector.checkClass(InvalidPackageDetector.java:110)←AsmVisitor.runClassDetectors(AsmVisitor.java:154)←LintDriver.runClassDetectors(LintDriver.kt:1408)←LintDriver.checkClasses(LintDriver.kt:1276)←LintDriver.runFileDetectors(LintDriver.kt:1044)←LintDriver.checkProject(LintDriver.kt:850)←LintDriver.analyze(LintDriver.kt:378)←LintCliClient.run(LintCliClient.java:238)`&#xA;&#xA;You can set environment variable `LINT_PRINT_STACKTRACE=true` to dump a full stacktrace to stdout."
			val explanation =
				"This issue type represents a problem running lint itself. Examples include failure to find bytecode for source files (which means certain detectors could not be run), parsing errors in lint configuration files, etc.&#xA;&#xA;These errors are not errors in your own code, but they are shown to make it clear that some checks were not completed."
			setField("message", "$summary\n$message\n$explanation")
		})

		val result = model.messaging

		assertEquals(
			"""
			Lint Failure
			""".trimIndent(),
			result.title
		)
		assertEquals(
			"""
			Unexpected failure during lint analysis of \`module-info.class\` (this is a bug in lint or one of the libraries it depends on)
			
			\`\`\`
			Exception in thread "lint" NullPointerException:
				at InvalidPackageDetector.checkClass(InvalidPackageDetector.java:110)
				at AsmVisitor.runClassDetectors(AsmVisitor.java:154)
				at LintDriver.runClassDetectors(LintDriver.kt:1408)
				at LintDriver.checkClasses(LintDriver.kt:1276)
				at LintDriver.runFileDetectors(LintDriver.kt:1044)
				at LintDriver.checkProject(LintDriver.kt:850)
				at LintDriver.analyze(LintDriver.kt:378)
				at LintCliClient.run(LintCliClient.java:238)
			\`\`\`
			""".trimIndent(),
			result.message
		)
		assertEquals(
			"""
			You can set environment variable \`LINT_PRINT_STACKTRACE=true\` to dump a full stacktrace to stdout.
			
			This issue type represents a problem running lint itself. Examples include failure to find bytecode for source files (which means certain detectors could not be run), parsing errors in lint configuration files, etc.
			
			These errors are not errors in your own code, but they are shown to make it clear that some checks were not completed.
			""".trimIndent(),
			result.description
		)
	}

	class Suppressions {

		private val fixture = createAndroidLintFixture()

		@Test
		fun `unknown files are suppressed with lint-xml`() {
			val model = DetailsViewModel(fixture.build<Violation>().apply {
				// this.location.file is fixture'd
			})

			val result = model.suppression

			assertThat(result, containsString("""<issue id="${model.rule}" severity="ignore">"""))
		}

		@Test
		fun `java files are suppressed with annotation`() {
			val model = DetailsViewModel(fixture.build<Violation>().apply {
				this.location.setField("file", File(fixture.build<String>() + ".java"))
			})

			val result = model.suppression

			assertThat(result, containsString("""@SuppressLint("${model.rule}")"""))
		}
	}

	companion object {

		private fun createAndroidLintFixture(): JFixture {
			return JFixture().apply {
				customise().lazyInstance(Project::class.java) {
					project(":" + build())
				}
				customise().lazyInstance(Task::class.java) { mock() }
				customise().intercept(Violation::class.java) {
					it.source.setField("reporter", "ANDROIDLINT")
				}
			}
		}
	}
}
