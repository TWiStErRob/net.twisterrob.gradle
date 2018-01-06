package net.twisterrob.gradle.test

import org.gradle.testkit.runner.BuildResult
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

import static org.mockito.Mockito.when

class BuildResultExtensionsTest {

	static String NL = System.lineSeparator()
	static String SAMPLE_OUTPUT = //@formatter:off
	NL + "FAILURE: Build failed with an exception." +
	NL + "" +
	NL + "* What went wrong:" +
	NL + "Task 'checkstyle' is ambiguous in root project 'temp'."+
	" Candidates are: 'checkstyleMain', 'checkstyleTest'." +
	NL + "" +
	NL + "* Try:" +
	NL + "Run gradle tasks to get a list of available tasks."+
	" Run with --info or --debug option to get more log output." +
	NL + "" +
	NL + "* Exception is:" +
	NL + "org.gradle.execution.TaskSelectionException:"+
	" Task 'checkstyle' is ambiguous in root project 'temp'."+
	" Candidates are: 'checkstyleMain', 'checkstyleTest'." +
	NL + "\tat org.gradle.execution.TaskSelector.getSelection(TaskSelector.java:116)" +
	NL + "\tat org.gradle.execution.TaskSelector.getSelection(TaskSelector.java:81)" +
	NL + "\tat org.gradle.execution.commandline.CommandLineTaskParser.parseTasks(CommandLineTaskParser.java:42)" +
	NL + "" +
	NL + "" +
	NL + "* Get more help at https://help.gradle.org" +
	NL + "" +
	NL + "BUILD FAILED in 1s" +
	NL + ""
	//@formatter:on

	@Mock BuildResult mockResult

	@Before void setUp() {
		MockitoAnnotations.initMocks(this)
		when(mockResult.output).thenReturn(SAMPLE_OUTPUT)
	}

	@Test void failReason() {
		assert BuildResultExtensions.failReason(mockResult) ==
				"Task 'checkstyle' is ambiguous in root project 'temp'." +
				" Candidates are: 'checkstyleMain', 'checkstyleTest'."
	}

	@Test void failSuggestion() {
		assert BuildResultExtensions.failSuggestion(mockResult) ==
				"Run gradle tasks to get a list of available tasks." +
				" Run with --info or --debug option to get more log output."
	}

	@Test void fullException() {
		assert BuildResultExtensions.fullException(mockResult) == //@formatter:off
		"org.gradle.execution.TaskSelectionException:"+
		" Task 'checkstyle' is ambiguous in root project 'temp'."+
		" Candidates are: 'checkstyleMain', 'checkstyleTest'." +
		NL + "\tat org.gradle.execution.TaskSelector.getSelection(TaskSelector.java:116)" +
		NL + "\tat org.gradle.execution.TaskSelector.getSelection(TaskSelector.java:81)" +
		NL + "\tat org.gradle.execution.commandline.CommandLineTaskParser.parseTasks(CommandLineTaskParser.java:42)"
		//@formatter:on
	}
}
