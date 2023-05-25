package net.twisterrob.gradle.test

import org.gradle.testkit.runner.BuildResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class BuildResultExtensionsTest {

	companion object {

		val SAMPLE_OUTPUT: String = """
			
			FAILURE: Build failed with an exception.
			
			* What went wrong:
			Task 'checkstyle' is ambiguous in root project 'temp'. Candidates are: 'checkstyleMain', 'checkstyleTest'.
			
			* Try:
			Run gradle tasks to get a list of available tasks. Run with --info or --debug option to get more log output.
			
			* Exception is:
			org.gradle.execution.TaskSelectionException: Task 'checkstyle' is ambiguous in root project 'temp'. Candidates are: 'checkstyleMain', 'checkstyleTest'.
				at org.gradle.execution.TaskSelector.getSelection(TaskSelector.java:116)
				at org.gradle.execution.TaskSelector.getSelection(TaskSelector.java:81)
				at org.gradle.execution.commandline.CommandLineTaskParser.parseTasks(CommandLineTaskParser.java:42)
			
			
			* Get more help at https://help.gradle.org
			
			BUILD FAILED in 1s
			
		""".trimIndent().replace("\n", System.lineSeparator())
	}

	@Mock lateinit var mockResult: BuildResult

	@BeforeEach fun setUp() {
		whenever(mockResult.output).thenReturn(SAMPLE_OUTPUT)
	}

	@Test fun failReason() {
		assertEquals(
			mockResult.failReason,
			"Task 'checkstyle' is ambiguous in root project 'temp'."
					+ " Candidates are: 'checkstyleMain', 'checkstyleTest'."
		)
	}

	@Test fun failSuggestion() {
		assertEquals(
			mockResult.failSuggestion,
			"Run gradle tasks to get a list of available tasks."
					+ " Run with --info or --debug option to get more log output."
		)
	}

	@Test fun fullException() {
		assertEquals(
			mockResult.fullException,
			"""
				org.gradle.execution.TaskSelectionException: Task 'checkstyle' is ambiguous in root project 'temp'. Candidates are: 'checkstyleMain', 'checkstyleTest'.
					at org.gradle.execution.TaskSelector.getSelection(TaskSelector.java:116)
					at org.gradle.execution.TaskSelector.getSelection(TaskSelector.java:81)
					at org.gradle.execution.commandline.CommandLineTaskParser.parseTasks(CommandLineTaskParser.java:42)
			""".trimIndent().replace("\n", System.lineSeparator())
		)
	}
}
