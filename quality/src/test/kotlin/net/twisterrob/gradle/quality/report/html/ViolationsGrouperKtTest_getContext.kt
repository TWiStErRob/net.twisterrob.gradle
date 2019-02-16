package net.twisterrob.gradle.quality.report.html

import net.twisterrob.gradle.quality.Violation
import net.twisterrob.gradle.quality.Violation.Location
import net.twisterrob.gradle.quality.Violation.Severity.ERROR
import net.twisterrob.gradle.quality.Violation.Source
import net.twisterrob.gradle.quality.report.html.model.ContextViewModel
import org.gradle.api.Project
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito.mock
import java.io.File

class ViolationsGrouperKtTest_getContext {
	@Rule @JvmField val temp = TemporaryFolder()

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

	private fun lines(start: Int, end: Int): String = (start..end).joinToString(System.lineSeparator()) { "line$it" }

	private fun runTest(input: String, requestedStart: Int, requestedEnd: Int, expectedStart: Int, expectedEnd: Int) {
		val origin = temp.newFile().apply { writeText(input) }

		val (context, start, end) = ContextViewModel.CodeContext.getContext(
			Violation(
				"", null, ERROR, "", emptyMap(),
				Location(mock(Project::class.java), "", origin, requestedStart, requestedEnd, 0),
				Source("", "", "", "", File("."), null)
			)
		)

		assertEquals(lines(expectedStart, expectedEnd), context)
		assertEquals(expectedStart, start)
		assertEquals(expectedEnd, end)
	}
}
