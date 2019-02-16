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

		@Test fun `clearAfterSuccess triggers by default`() {
			sut.evaluate()

			assertProjectFolderMissing()
		}

		@Nested
		inner class `clearAfterSuccess = false` {

			@BeforeEach fun setUp() {
				gradle.clearAfterSuccess = false
			}

			@Test fun `keeps project folder`() {
				sut.evaluate()

				assertProjectFolderValid()
			}

			@ValueSource(strings = ["true", "false"])
			@ParameterizedTest fun `even when clearAfterFailure = `(clearAfterFailure: Boolean) {
				gradle.clearAfterFailure = clearAfterFailure

				sut.evaluate()

				assertProjectFolderValid()
			}
		}

		@Nested
		inner class `clearAfterSuccess = true` {

			@BeforeEach fun setUp() {
				gradle.clearAfterSuccess = true
			}

			@Test fun `removes project folder`() {
				sut.evaluate()

				assertProjectFolderMissing()
			}

			@ValueSource(strings = ["true", "false"])
			@ParameterizedTest fun `even when clearAfterFailure = `(clearAfterFailure: Boolean) {
				gradle.clearAfterFailure = clearAfterFailure

				sut.evaluate()

				assertProjectFolderMissing()
			}
		}

		@Nested
		inner class `clearAfterSuccess set through System property` {

			private var clearAfterSuccessProperty = SystemProperty("net.twisterrob.gradle.runner.clearAfterSuccess")

			@BeforeEach fun setUp() {
				clearAfterSuccessProperty.backup()
			}

			@AfterEach fun tearDown() {
				clearAfterSuccessProperty.restore()
			}

			@Test fun `'true' removes project folder`() {
				clearAfterSuccessProperty.set("true")

				sut.evaluate()

				assertProjectFolderMissing()
			}

			@Test fun `'false' keeps project folder`() {
				clearAfterSuccessProperty.set("false")

				sut.evaluate()

				assertProjectFolderValid()
			}
		}
	}

	@Nested
	inner class `when test method fails` {

		@BeforeEach fun setUp() {
			doThrow(SimulatedTestFailure()).whenever(innerStatement).evaluate()
		}

		@Test fun `clearAfterFailure triggers by default`() {
			assertThrows<SimulatedTestFailure> {
				sut.evaluate()
			}

			assertProjectFolderMissing()
		}

		@Nested
		inner class `clearAfterFailure = false` {

			@BeforeEach fun setUp() {
				gradle.clearAfterFailure = false
			}

			@Test
			fun `keeps project folder`() {
				assertThrows<SimulatedTestFailure> {
					sut.evaluate()
				}

				assertProjectFolderValid()
			}

			@ValueSource(strings = ["true", "false"])
			@ParameterizedTest fun `even when clearAfterSuccess = `(clearAfterSuccess: Boolean) {
				gradle.clearAfterSuccess = clearAfterSuccess

				assertThrows<SimulatedTestFailure> {
					sut.evaluate()
				}

				assertProjectFolderValid()
			}
		}

		@Nested
		inner class `clearAfterFailure = true` {

			@BeforeEach fun setUp() {
				gradle.clearAfterFailure = true
			}

			@Test fun `removes project folder`() {
				assertThrows<SimulatedTestFailure> {
					sut.evaluate()
				}

				assertProjectFolderMissing()
			}

			@ValueSource(strings = ["true", "false"])
			@ParameterizedTest fun `even when clearAfterSuccess = `(clearAfterSuccess: Boolean) {
				// make sure clearAfterFailure is independent of clearAfterSuccess
				gradle.clearAfterSuccess = clearAfterSuccess

				assertThrows<SimulatedTestFailure> {
					sut.evaluate()
				}

				assertProjectFolderMissing()
			}
		}

		@Nested
		inner class `clearAfterFailure set through System property` {

			private var clearAfterFailureProperty = SystemProperty("net.twisterrob.gradle.runner.clearAfterFailure")

			@BeforeEach fun setUp() {
				clearAfterFailureProperty.backup()
			}

			@AfterEach fun tearDown() {
				clearAfterFailureProperty.restore()
			}

			@Test fun `'true' removes project folder`() {
				clearAfterFailureProperty.set("true")

				assertThrows<SimulatedTestFailure> {
					sut.evaluate()
				}

				assertProjectFolderMissing()
			}

			@Test fun `'false' keeps project folder`() {
				clearAfterFailureProperty.set("false")

				assertThrows<SimulatedTestFailure> {
					sut.evaluate()
				}

				assertProjectFolderValid()
			}
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

	private class SystemProperty(private val key: String) {
		private var backupValue: String? = null

		fun set(value: String?) {
			if (value == null) {
				System.clearProperty(key)
			} else {
				System.setProperty(key, value)
			}
		}

		fun backup() {
			backupValue = System.getProperty(key)
		}

		fun restore() {
			set(backupValue)
		}
	}
}
