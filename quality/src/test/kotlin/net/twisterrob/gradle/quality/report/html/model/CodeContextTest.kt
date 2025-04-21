package net.twisterrob.gradle.quality.report.html.model

import com.flextrade.jfixture.JFixture
import net.twisterrob.gradle.quality.Violation
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.matchesPattern
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.FileNotFoundException

@Suppress("detekt.NamedArguments")
class CodeContextTest {

	@TempDir lateinit var temp: File

	private val fixture = JFixture()

	@Nested
	inner class MissingLocation {

		private val model = ContextViewModel.CodeContext(
			fixture.build<Violation>().apply {
				location.setField("file", File("non-existent.file"))
			}
		)

		@Test
		fun `render exception when violation points to a missing location`() {
			val ex = assertThrows<FileNotFoundException> { model.data }

			assertThat(
				ex.message,
				matchesPattern(""".*non-existent\.file.*""")
			)
		}

		@Test
		fun `render exception with full path when violation points to a missing location`() {
			val ex = assertThrows<FileNotFoundException> { model.data }

			assertThat(
				ex.message,
				matchesPattern(""".+non-existent\.file.*""")
			)
		}

		@Test
		fun `send invalid start and end lines when violation points to a missing location`() {
			assertThrows<FileNotFoundException> { model.startLine }
			assertThrows<FileNotFoundException> { model.endLine }
		}
	}

	@Test
	fun `grabs all 1 lines as requested`() {
		runTest(lines(1, 1), 1, 1, 1, 1)
	}

	@Test
	fun `grabs all 2 lines as requested`() {
		runTest(lines(1, 2), 1, 2, 1, 2)
	}

	@Test
	fun `grabs 3 lines as requested`() {
		runTest(lines(1, 9), 4, 6, 2, 8)
	}

	@Test
	fun `grabs 3 lines as requested (reversed)`() {
		runFailTest(lines(1, 9), 6, 4)
	}

	@Test
	fun `grabs 4 lines as requested`() {
		runTest(lines(1, 6), 2, 5, 1, 6)
	}

	@Test
	fun `grabs all 6 lines as requested`() {
		runTest(lines(1, 6), 1, 6, 1, 6)
	}

	@Test
	fun `grabs all 10 lines as requested`() {
		runTest(lines(1, 10), 1, 10, 1, 10)
	}

	@Test
	fun `grabs 1 line with 2 lines of context`() {
		runTest(lines(1, 6), 4, 4, 2, 6)
	}

	@Test
	fun `grabs 2 lines with 2 lines of context (odd)`() {
		runTest(lines(1, 8), 3, 4, 1, 6)
	}

	@Test
	fun `grabs 2 lines with 2 lines of context (even)`() {
		runTest(lines(1, 8), 4, 5, 2, 7)
	}

	@Test
	fun `bounded context by file size`() {
		runTest(lines(1, 3), 2, 2, 1, 3)
	}

	@Test
	fun `lower bounded context by file size (neighbor)`() {
		runTest(lines(1, 8), 2, 2, 1, 4)
	}

	@Test
	fun `lower bounded context by file size (direct)`() {
		runTest(lines(1, 8), 1, 1, 1, 3)
	}

	@Test
	fun `lower bounded context by file size (windows peek in)`() {
		runFailTest(lines(1, 6), -1, -1)
	}

	@Test
	fun `lower bounded context by file size (underflow)`() {
		runFailTest(lines(1, 6), -8, -8)
	}

	@Test
	fun `lower bounded context by file size (underflow valid range)`() {
		runFailTest(lines(1, 6), -8, -6)
	}

	@Test
	fun `lower bounded context by file size (underflow invalid range)`() {
		runFailTest(lines(1, 6), -4, -6)
	}

	@Test
	fun `upper bounded context by file size (neighbor)`() {
		runTest(lines(1, 8), 2, 2, 1, 4)
	}

	@Test
	fun `upper bounded context by file size (direct)`() {
		runTest(lines(1, 8), 8, 8, 6, 8)
	}

	@Test
	fun `upper bounded context by file size (windows peek in)`() {
		runFailTest(lines(1, 6), 8, 8)
	}

	@Test
	fun `upper bounded context by file size (overflow)`() {
		runFailTest(lines(1, 6), 12, 12)
	}

	@Test
	fun `upper bounded context by file size (overflow valid range)`() {
		runFailTest(lines(1, 6), 12, 15)
	}

	@Test
	fun `upper bounded context by file size (overflow invalid range)`() {
		runFailTest(lines(1, 6), 15, 12)
	}

	@Test
	fun `bounded context by file size (neighbor valid range)`() {
		runFailTest(lines(1, 6), 0, 7)
	}

	@Test
	fun `bounded context by file size (neighbor invalid range)`() {
		runFailTest(lines(1, 6), 7, 0)
	}

	private fun lines(start: Int, end: Int): String =
		(start..end).joinToString(System.lineSeparator()) { "line$it" }

	private fun createModel(origin: File, requestedStart: Int, requestedEnd: Int): ContextViewModel.CodeContext =
		ContextViewModel.CodeContext(
			fixture.build<Violation>().apply {
				location.setField("file", origin)
				location.setField("startLine", requestedStart)
				location.setField("endLine", requestedEnd)
			}
		)

	private fun runTest(input: String, requestedStart: Int, requestedEnd: Int, expectedStart: Int, expectedEnd: Int) {
		val origin = temp.resolve("source.file").apply { writeText(input) }

		val model = createModel(origin, requestedStart, requestedEnd)

		assertEquals(lines(expectedStart, expectedEnd), model.data)
		assertEquals(expectedStart, model.startLine)
		assertEquals(expectedEnd, model.endLine)
	}

	private fun runFailTest(input: String, requestedStart: Int, requestedEnd: Int) {
		val origin = temp.resolve("source.file").apply { writeText(input) }

		val model = createModel(origin, requestedStart, requestedEnd)
		val ex = assertThrows<IllegalStateException> { model.data }

		val fileName = ".*${Regex.escape(origin.name)}"
		assertThat(
			ex.message,
			matchesPattern("""Invalid location in ${fileName}: requested ${requestedStart} to ${requestedEnd}\b.*""")
		)
		assertThrows<IllegalStateException> { model.startLine }
		assertThrows<IllegalStateException> { model.endLine }
	}
}
