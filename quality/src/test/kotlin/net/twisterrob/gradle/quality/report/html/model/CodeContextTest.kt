package net.twisterrob.gradle.quality.report.html.model

import com.flextrade.jfixture.JFixture
import net.twisterrob.gradle.quality.Violation
import org.gradle.api.Project
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.matchesPattern
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class CodeContextTest {

	@Rule @JvmField val temp = TemporaryFolder()

	private val fixture = JFixture().apply {
		customise().lazyInstance(Project::class.java) {
			project(":" + build())
		}
	}

	class MissingLocation {
		private val fixture = JFixture().apply {
			customise().lazyInstance(Project::class.java) {
				project(":" + build())
			}
		}

		private val model = ContextViewModel.CodeContext(
			fixture.build<Violation>().apply {
				location.setField("file", File("non-existent.file"))
			}
		)

		@Test
		fun `render exception when violation points to a missing location`() {
			assertThat(
				model.data,
				matchesPattern("""java\.io\.FileNotFoundException: .*non-existent\.file.*""")
			)
		}

		@Test
		fun `render exception with full path when violation points to a missing location`() {
			assertThat(
				model.data,
				matchesPattern("""java\.io\.FileNotFoundException: .+non-existent\.file.*""")
			)
		}

		@Test
		fun `send invalid start and end lines when violation points to a missing location`() {
			assertEquals(0, model.startLine)
			assertEquals(0, model.endLine)
		}
	}

	@Test
	fun `grabs 3 lines as requested`() =
		runTest(lines(1, 9), 4, 6, 2, 8)

	@Test
	fun `grabs 4 lines as requested`() =
		runTest(lines(1, 6), 2, 5, 1, 6)

	@Test
	fun `grabs 1 line with 2 lines of context`() =
		runTest(lines(1, 6), 4, 4, 2, 6)

	@Test
	fun `grabs 2 lines with 2 lines of context (odd)`() =
		runTest(lines(1, 8), 3, 4, 1, 6)

	@Test
	fun `grabs 2 lines with 2 lines of context (even)`() =
		runTest(lines(1, 8), 4, 5, 2, 7)

	@Test
	fun `bounded context by file size`() =
		runTest(lines(1, 3), 2, 2, 1, 3)

	@Test
	fun `lower bounded context by file size (neighbor)`() =
		runTest(lines(1, 8), 2, 2, 1, 4)

	@Test
	fun `lower bounded context by file size (direct)`() =
		runTest(lines(1, 8), 1, 1, 1, 3)

	@Test
	fun `upper bounded context by file size (neighbor)`() =
		runTest(lines(1, 8), 2, 2, 1, 4)

	@Test
	fun `upper bounded context by file size (direct)`() =
		runTest(lines(1, 8), 8, 8, 6, 8)

	private fun lines(start: Int, end: Int): String =
		(start..end).joinToString(System.lineSeparator()) { "line$it" }

	private fun runTest(input: String, requestedStart: Int, requestedEnd: Int, expectedStart: Int, expectedEnd: Int) {
		val origin = temp.newFile().apply { writeText(input) }

		val model = ContextViewModel.CodeContext(
			fixture.build<Violation>().apply {
				location.setField("file", origin)
				location.setField("startLine", requestedStart)
				location.setField("endLine", requestedEnd)
			}
		)

		assertEquals(lines(expectedStart, expectedEnd), model.data)
		assertEquals(expectedStart, model.startLine)
		assertEquals(expectedEnd, model.endLine)
	}
}
