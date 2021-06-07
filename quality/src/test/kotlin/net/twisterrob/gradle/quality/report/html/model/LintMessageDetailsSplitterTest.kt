package net.twisterrob.gradle.quality.report.html.model

import com.flextrade.jfixture.JFixture
import net.twisterrob.gradle.quality.Violation
import org.gradle.api.Project
import org.gradle.api.Task
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import kotlin.test.assertEquals

class LintMessageDetailsSplitterTest {

	private val fixture = createAndroidLintFixture()

	private val sut = LintMessageDetailsSplitter()

	@Test
	fun `IconMissingDensityFolder specific message escapes are removed`() {
		val input = fixture.build<Violation>().apply {
			setField("rule", "IconMissingDensityFolder")
			val lintMessage = """
				Title
				Missing density variation folders in `src\\main\\res`: drawable-hdpi
			""".trimIndent()
			setField("message", lintMessage)
		}

		val output = sut.split(input)

		assertEquals("""Missing density variation folders in `src\main\res`: drawable-hdpi""", output.message)
	}

	@Test
	fun `LintError specific message gets formatted`() {
		val input = fixture.build<Violation>().apply {
			setField("rule", "LintError")
			val summary = "Lint Failure"
			val message =
				"Unexpected failure during lint analysis of module-info.class (this is a bug in lint or one of the libraries it depends on)&#xA;&#xA;Stack: `NullPointerException:InvalidPackageDetector.checkClass(InvalidPackageDetector.java:110)←AsmVisitor.runClassDetectors(AsmVisitor.java:154)←LintDriver.runClassDetectors(LintDriver.kt:1408)←LintDriver.checkClasses(LintDriver.kt:1276)←LintDriver.runFileDetectors(LintDriver.kt:1044)←LintDriver.checkProject(LintDriver.kt:850)←LintDriver.analyze(LintDriver.kt:378)←LintCliClient.run(LintCliClient.java:238)`&#xA;&#xA;You can set environment variable `LINT_PRINT_STACKTRACE=true` to dump a full stacktrace to stdout."
			val explanation =
				"This issue type represents a problem running lint itself. Examples include failure to find bytecode for source files (which means certain detectors could not be run), parsing errors in lint configuration files, etc.&#xA;&#xA;These errors are not errors in your own code, but they are shown to make it clear that some checks were not completed."
			setField("message", "$summary\n$message\n$explanation")
		}

		val output = sut.split(input)

		assertEquals(
			"""
			Lint Failure
			""".trimIndent(),
			output.title
		)
		assertEquals(
			"""
			Unexpected failure during lint analysis of `module-info.class`.
			
			```
			Exception in thread "lint" NullPointerException:
				at InvalidPackageDetector.checkClass(InvalidPackageDetector.java:110)
				at AsmVisitor.runClassDetectors(AsmVisitor.java:154)
				at LintDriver.runClassDetectors(LintDriver.kt:1408)
				at LintDriver.checkClasses(LintDriver.kt:1276)
				at LintDriver.runFileDetectors(LintDriver.kt:1044)
				at LintDriver.checkProject(LintDriver.kt:850)
				at LintDriver.analyze(LintDriver.kt:378)
				at LintCliClient.run(LintCliClient.java:238)
			```
			""".trimIndent(),
			output.message
		)
		assertEquals(
			"""
			This is a bug in lint or one of the libraries it depends on.
			
			You can set environment variable `LINT_PRINT_STACKTRACE=true` to dump a full stacktrace to stdout.
			
			This issue type represents a problem running lint itself. Examples include failure to find bytecode for source files (which means certain detectors could not be run), parsing errors in lint configuration files, etc.
			
			These errors are not errors in your own code, but they are shown to make it clear that some checks were not completed.
			""".trimIndent(),
			output.description
		)
	}

	@Test
	fun `LintError specific message gets formatted no stack`() {
		val input = fixture.build<Violation>().apply {
			setField("rule", "LintError")
			val summary = "Lint Failure"
			val message =
				"Unexpected failure during lint analysis of module-info.class (this is a bug in lint or one of the libraries it depends on)&#xA;&#xA;Stack: `NullPointerException:`&#xA;&#xA;You can set environment variable `LINT_PRINT_STACKTRACE=true` to dump a full stacktrace to stdout."
			val explanation =
				"This issue type represents a problem running lint itself. Examples include failure to find bytecode for source files (which means certain detectors could not be run), parsing errors in lint configuration files, etc.&#xA;&#xA;These errors are not errors in your own code, but they are shown to make it clear that some checks were not completed."
			setField("message", "$summary\n$message\n$explanation")
		}

		val output = sut.split(input)

		assertEquals(
			"""
			Lint Failure
			""".trimIndent(),
			output.title
		)
		assertEquals(
			"""
			`NullPointerException` during lint analysis of `module-info.class`.
			""".trimIndent(),
			output.message
		)
		assertEquals(
			"""
			This is a bug in lint or one of the libraries it depends on.
			
			You can set environment variable `LINT_PRINT_STACKTRACE=true` to dump a full stacktrace to stdout.
			
			This issue type represents a problem running lint itself. Examples include failure to find bytecode for source files (which means certain detectors could not be run), parsing errors in lint configuration files, etc.
			
			These errors are not errors in your own code, but they are shown to make it clear that some checks were not completed.
			""".trimIndent(),
			output.description
		)
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
