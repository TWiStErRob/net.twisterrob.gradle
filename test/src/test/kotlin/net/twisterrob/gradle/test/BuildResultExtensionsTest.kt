package net.twisterrob.gradle.test

import com.nhaarman.mockitokotlin2.whenever
import org.gradle.testkit.runner.BuildResult
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class BuildResultExtensionsTest {

	companion object {

		val NL = System.lineSeparator()!!
		val SAMPLE_OUTPUT = (NL
				+ "FAILURE: Build failed with an exception." + NL
				+ "" + NL
				+ "* What went wrong:" + NL
				+ "Task 'checkstyle' is ambiguous in root project 'temp'."
				+ " Candidates are: 'checkstyleMain', 'checkstyleTest'." + NL
				+ "" + NL
				+ "* Try:" + NL
				+ "Run gradle tasks to get a list of available tasks."
				+ " Run with --info or --debug option to get more log output." + NL
				+ "" + NL
				+ "* Exception is:" + NL
				+ "org.gradle.execution.TaskSelectionException:"
				+ " Task 'checkstyle' is ambiguous in root project 'temp'."
				+ " Candidates are: 'checkstyleMain', 'checkstyleTest'." + NL
				+ "\tat org.gradle.execution.TaskSelector.getSelection(TaskSelector.java:116)" + NL
				+ "\tat org.gradle.execution.TaskSelector.getSelection(TaskSelector.java:81)" + NL
				+ "\tat org.gradle.execution.commandline.CommandLineTaskParser.parseTasks(CommandLineTaskParser.java:42)" + NL
				+ "" + NL
				+ "" + NL
				+ "* Get more help at https://help.gradle.org" + NL
				+ "" + NL
				+ "BUILD FAILED in 1s" + NL
				)
	}

	@Mock lateinit var mockResult: BuildResult

	@BeforeEach fun setUp() {
		whenever(mockResult.output).thenReturn(SAMPLE_OUTPUT)
	}

	@Test fun failReason() {
		assertEquals(mockResult.failReason,
				"Task 'checkstyle' is ambiguous in root project 'temp'."
						+ " Candidates are: 'checkstyleMain', 'checkstyleTest'.")
	}

	@Test fun failSuggestion() {
		assertEquals(mockResult.failSuggestion,
				"Run gradle tasks to get a list of available tasks."
						+ " Run with --info or --debug option to get more log output.")
	}

	@Test fun fullException() {
		assertEquals(mockResult.fullException, "org.gradle.execution.TaskSelectionException:"
				+ " Task 'checkstyle' is ambiguous in root project 'temp'."
				+ " Candidates are: 'checkstyleMain', 'checkstyleTest'." + NL
				+ "\tat org.gradle.execution.TaskSelector.getSelection(TaskSelector.java:116)" + NL
				+ "\tat org.gradle.execution.TaskSelector.getSelection(TaskSelector.java:81)" + NL
				+ "\tat org.gradle.execution.commandline.CommandLineTaskParser.parseTasks(CommandLineTaskParser.java:42)"
		)
	}
}
