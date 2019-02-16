package net.twisterrob.gradle.test

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.not
import org.hamcrest.io.FileMatchers.anExistingFileOrDirectory
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.rules.TestRule
import org.junit.runners.model.Statement
import kotlin.test.assertTrue

class GradleRunnerRuleTest {

	@Nested
	inner class ExplicitRuleTests {

		@Nested
		inner class WhenTestMethodSucceeds {

			@ValueSource(strings = ["true", "false"])
			@ParameterizedTest fun `clearAfterSuccess = false keeps project folder on success`(clearAfterFailure: Boolean) {
				val gradle = GradleRunnerRule()
				// make sure clearAfterSuccess is independent of clearAfterFailure
				gradle.clearAfterFailure = clearAfterFailure
				val testStatement = mock<Statement>()

				whenever(testStatement.evaluate()).thenAnswer {
					// check if files are there to begin with
					assertThat(gradle.runner.projectDir, anExistingFileOrDirectory())
					assertThat(gradle.buildFile, anExistingFileOrDirectory())
				}
				val statement = (gradle as TestRule).apply(testStatement, mock())

				gradle.clearAfterSuccess = false
				statement.evaluate()

				// verify
				verify(testStatement).evaluate()
				assertThat(gradle.runner.projectDir, anExistingFileOrDirectory())
				assertThat(gradle.buildFile, anExistingFileOrDirectory())

				// clean up
				assertTrue(gradle.runner.projectDir.deleteRecursively())
			}

			@ValueSource(strings = ["true", "false"])
			@ParameterizedTest fun `clearAfterSuccess = true removes project folder on failure`(clearAfterFailure: Boolean) {
				val gradle = GradleRunnerRule()
				// make sure clearAfterSuccess is independent of clearAfterFailure
				gradle.clearAfterFailure = clearAfterFailure
				val testStatement = mock<Statement>()

				whenever(testStatement.evaluate()).thenAnswer {
					// check if files are there to begin with
					assertThat(gradle.runner.projectDir, anExistingFileOrDirectory())
					assertThat(gradle.buildFile, anExistingFileOrDirectory())
				}
				val statement = (gradle as TestRule).apply(testStatement, mock())

				gradle.clearAfterSuccess = true
				statement.evaluate()

				// verify
				verify(testStatement).evaluate()
				assertThat(gradle.runner.projectDir, not(anExistingFileOrDirectory()))
				assertThat(gradle.buildFile, not(anExistingFileOrDirectory()))
			}
		}

		@Nested
		inner class WhenTestMethodFailes {

			@ValueSource(strings = ["true", "false"])
			@ParameterizedTest fun `clearAfterFailure = false keeps project folder on failure`(clearAfterSuccess: Boolean) {
				val gradle = GradleRunnerRule()
				// make sure clearAfterFailure is independent of clearAfterSuccess
				gradle.clearAfterSuccess = clearAfterSuccess
				val testStatement = mock<Statement>()

				class SimulatedTestFailure : AssertionError()
				whenever(testStatement.evaluate()).thenAnswer {
					// check if files are there to begin with
					assertThat(gradle.runner.projectDir, anExistingFileOrDirectory())
					assertThat(gradle.buildFile, anExistingFileOrDirectory())
					throw SimulatedTestFailure()
				}
				val statement = (gradle as TestRule).apply(testStatement, mock())

				gradle.clearAfterFailure = false
				assertThrows<SimulatedTestFailure> {
					statement.evaluate()
				}

				// verify
				verify(testStatement).evaluate()
				assertThat(gradle.runner.projectDir, anExistingFileOrDirectory())
				assertThat(gradle.buildFile, anExistingFileOrDirectory())

				// clean up
				assertTrue(gradle.runner.projectDir.deleteRecursively())
			}

			@ValueSource(strings = ["true", "false"])
			@ParameterizedTest fun `clearAfterFailure = true removes project folder on failure`(clearAfterSuccess: Boolean) {
				val gradle = GradleRunnerRule()
				// make sure clearAfterFailure is independent of clearAfterSuccess
				gradle.clearAfterSuccess = clearAfterSuccess
				val testStatement = mock<Statement>()

				class SimulatedTestFailure : AssertionError()
				whenever(testStatement.evaluate()).thenAnswer {
					// check if files are there to begin with
					assertThat(gradle.runner.projectDir, anExistingFileOrDirectory())
					assertThat(gradle.buildFile, anExistingFileOrDirectory())
					throw SimulatedTestFailure()
				}
				val statement = (gradle as TestRule).apply(testStatement, mock())

				gradle.clearAfterFailure = true
				assertThrows<SimulatedTestFailure> {
					statement.evaluate()
				}

				// verify
				verify(testStatement).evaluate()
				assertThat(gradle.runner.projectDir, not(anExistingFileOrDirectory()))
				assertThat(gradle.buildFile, not(anExistingFileOrDirectory()))
			}
		}
	}
}
