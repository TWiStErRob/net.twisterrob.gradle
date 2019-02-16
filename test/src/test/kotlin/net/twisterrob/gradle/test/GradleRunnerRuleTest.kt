package net.twisterrob.gradle.test

import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.not
import org.hamcrest.io.FileMatchers.anExistingFileOrDirectory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.ParameterizedTest.ARGUMENTS_PLACEHOLDER
import org.junit.jupiter.params.provider.ValueSource
import org.junit.rules.TestRule
import org.junit.runners.model.Statement
import kotlin.test.assertTrue

class GradleRunnerRuleTest {

	class SimulatedTestFailure : AssertionError()

	private val gradle = GradleRunnerRule()
	private val innerStatement = mock<Statement>()
	private val sut: Statement = (gradle as TestRule).apply(innerStatement, mock())

	@AfterEach fun tearDown() {
		// clean up regardless of outcome
		assertTrue(gradle.runner.projectDir.deleteRecursively())
	}

	@Test fun `inner statement evaluates when Gradle rule's statement is evaluated`() {
		sut.evaluate()

		verify(innerStatement).evaluate()
	}

	@Test fun `project files exist during test execution`() {
		whenever(innerStatement.evaluate()).thenAnswer {
			// check if files are there to begin with
			assertProjectFolderValid()
		}

		sut.evaluate()

		// assertion in thenAnswer (because we need to check during execution)
	}

	@Nested
	inner class `when test method passes` {

		@BeforeEach fun setUp() {
			doNothing().whenever(innerStatement).evaluate()
		}

		@ValueSource(strings = ["true", "false"])
		@ParameterizedTest(name = "even when clearAfterFailure = $ARGUMENTS_PLACEHOLDER")
		fun `clearAfterSuccess = false keeps project folder`(clearAfterFailure: Boolean) {
			// make sure clearAfterSuccess is independent of clearAfterFailure
			gradle.clearAfterFailure = clearAfterFailure

			gradle.clearAfterSuccess = false
			sut.evaluate()

			assertProjectFolderValid()
		}

		@ValueSource(strings = ["true", "false"])
		@ParameterizedTest(name = "even when clearAfterFailure = $ARGUMENTS_PLACEHOLDER")
		fun `clearAfterSuccess = true removes project folder`(clearAfterFailure: Boolean) {
			// make sure clearAfterSuccess is independent of clearAfterFailure
			gradle.clearAfterFailure = clearAfterFailure

			gradle.clearAfterSuccess = true
			sut.evaluate()

			assertProjectFolderMissing()
		}
	}

	@Nested
	inner class `when test method fails` {

		@BeforeEach fun setUp() {
			doThrow(SimulatedTestFailure()).whenever(innerStatement).evaluate()
		}

		@ValueSource(strings = ["true", "false"])
		@ParameterizedTest(name = "even when clearAfterSuccess = $ARGUMENTS_PLACEHOLDER")
		fun `clearAfterFailure = false keeps project folder`(clearAfterSuccess: Boolean) {
			// make sure clearAfterFailure is independent of clearAfterSuccess
			gradle.clearAfterSuccess = clearAfterSuccess
			gradle.clearAfterFailure = false

			assertThrows<SimulatedTestFailure> {
				sut.evaluate()
			}

			assertProjectFolderValid()
		}

		@ValueSource(strings = ["true", "false"])
		@ParameterizedTest(name = "even when clearAfterSuccess = $ARGUMENTS_PLACEHOLDER")
		fun `clearAfterFailure = true removes project folder`(clearAfterSuccess: Boolean) {
			// make sure clearAfterFailure is independent of clearAfterSuccess
			gradle.clearAfterSuccess = clearAfterSuccess

			gradle.clearAfterFailure = true
			assertThrows<SimulatedTestFailure> {
				sut.evaluate()
			}

			assertProjectFolderMissing()
		}
	}

	private fun assertProjectFolderValid() {
		assertThat(gradle.runner.projectDir, anExistingFileOrDirectory())
		assertThat(gradle.buildFile, anExistingFileOrDirectory())
	}

	private fun assertProjectFolderMissing() {
		assertThat(gradle.runner.projectDir, not(anExistingFileOrDirectory()))
		assertThat(gradle.buildFile, not(anExistingFileOrDirectory()))
	}
}
