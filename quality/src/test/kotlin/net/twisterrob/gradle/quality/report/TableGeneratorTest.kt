package net.twisterrob.gradle.quality.report

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

class TableGeneratorTest {

	companion object {
		val INPUT: Map<String, Map<String, Map<String, Int?>>> = mapOf(
			"module1" to mapOf(
				"variant1" to mapOf(
					"checkstyle" to 1,
					"pmd" to 2
				),
				"variant2" to mapOf(
					"pmd" to 3
				)
			),
			"module2" to mapOf(
				"variant2" to emptyMap(),
				"variant3" to mapOf(
					"checkstyle" to 0,
					"unchecked" to null
				)
			),
			"module3" to mapOf(
				"variant2" to emptyMap()
			),
			"module4" to mapOf(
				"variant1" to mapOf(
					"checkstyle" to null,
					"pmd" to null,
				)
			),
		)

		val NO_COUNTS: Map<String, Map<String, Map<String, Int?>>> = mapOf(
			"module1" to mapOf("variant1" to mapOf("checker" to null)),
			"module2" to mapOf("variant2" to mapOf("checker" to null)),
			"module3" to mapOf("variant3" to mapOf("checker" to null)),
		)
	}

	@Test fun printSummary() {
		val sut = TableGenerator()

		val result = sut.build(INPUT)

		val expected = """
			module 	variant  	checkstyle	pmd	unchecked
			module1	variant1 	         1	  2	      N/A
			module1	variant2 	       N/A	  3	      N/A
			module2	variant2 	       N/A	N/A	      N/A
			module2	variant3 	         0	N/A	      N/A
			module3	variant2 	       N/A	N/A	      N/A
			module4	variant1 	       N/A	N/A	      N/A
			Summary	(total: 6)	         1	  5	      N/A
		""".trimIndent().normalizeLineEndings()
		assertEquals(expected, result)
	}

	@Test fun doesNotPrintEmptyRows() {
		val sut = TableGenerator(isPrintEmptyRows = false)

		val result = sut.build(INPUT)

		val expected = """
			module 	variant  	checkstyle	pmd	unchecked
			module1	variant1 	         1	  2	      N/A
			module1	variant2 	       N/A	  3	      N/A
			module2	variant3 	         0	N/A	      N/A
			Summary	(total: 6)	         1	  5	      N/A
		""".trimIndent().normalizeLineEndings()
		assertEquals(expected, result)
	}

	@Test fun doesNotPrintEmptyColumns() {
		val sut = TableGenerator(isPrintEmptyColumns = false)

		val result = sut.build(INPUT)

		val expected = """
			module 	variant  	checkstyle	pmd
			module1	variant1 	         1	  2
			module1	variant2 	       N/A	  3
			module2	variant2 	       N/A	N/A
			module2	variant3 	         0	N/A
			module3	variant2 	       N/A	N/A
			module4	variant1 	       N/A	N/A
			Summary	(total: 6)	         1	  5
		""".trimIndent().normalizeLineEndings()
		assertEquals(expected, result)
	}

	@Test fun doesNotPrintEmptyRowsAndColumns() {
		val sut = TableGenerator(isPrintEmptyRows = false, isPrintEmptyColumns = false)

		val result = sut.build(INPUT)

		val expected = """
			module 	variant  	checkstyle	pmd
			module1	variant1 	         1	  2
			module1	variant2 	       N/A	  3
			module2	variant3 	         0	N/A
			Summary	(total: 6)	         1	  5
		""".trimIndent().normalizeLineEndings()
		assertEquals(expected, result)
	}

	@CsvSource(
		"true, true",
		"true, false",
		"false, true",
		"false, false",
	)
	@ParameterizedTest fun `empty data (no modules)`(isPrintEmptyColumns: Boolean, isPrintEmptyRows: Boolean) {
		val sut = TableGenerator(isPrintEmptyColumns = isPrintEmptyColumns, isPrintEmptyRows = isPrintEmptyRows)

		val result = sut.build(emptyMap())

		val expected = """
			module 	variant${"   "}
			Summary	(total: 0)
		""".trimIndent().normalizeLineEndings()
		assertEquals(expected, result)
	}

	@CsvSource(
		"true, true",
		"true, false",
		"false, true",
		"false, false",
	)
	@ParameterizedTest fun `empty data (no variants)`(isPrintEmptyColumns: Boolean, isPrintEmptyRows: Boolean) {
		val sut = TableGenerator(isPrintEmptyColumns = isPrintEmptyColumns, isPrintEmptyRows = isPrintEmptyRows)

		val result = sut.build(
			mapOf(
				"module1" to emptyMap(),
				"module2" to emptyMap(),
				"module3" to emptyMap()
			)
		)

		val expected = """
			module 	variant${"   "}
			Summary	(total: 0)
		""".trimIndent().normalizeLineEndings()
		assertEquals(expected, result)
	}

	@ValueSource(booleans = [true, false])
	@ParameterizedTest fun `empty data (no parsers) - expanded`(isPrintEmptyColumns: Boolean) {
		val sut = TableGenerator(isPrintEmptyColumns = isPrintEmptyColumns, isPrintEmptyRows = true)

		val result = sut.build(
			mapOf(
				"module1" to mapOf("variant1" to emptyMap()),
				"module2" to mapOf("variant2" to emptyMap()),
				"module3" to mapOf("variant3" to emptyMap())
			)
		)

		val expected = """
			module 	variant${"   "}
			module1	variant1${"  "}
			module2	variant2${"  "}
			module3	variant3${"  "}
			Summary	(total: 0)
		""".trimIndent().normalizeLineEndings()
		assertEquals(expected, result)
	}

	@ValueSource(booleans = [true, false])
	@ParameterizedTest fun `empty data (no parsers) - collapsed`(isPrintEmptyColumns: Boolean) {
		val sut = TableGenerator(isPrintEmptyColumns = isPrintEmptyColumns, isPrintEmptyRows = false)

		val result = sut.build(
			mapOf(
				"module1" to mapOf("variant1" to emptyMap()),
				"module2" to mapOf("variant2" to emptyMap()),
				"module3" to mapOf("variant3" to emptyMap())
			)
		)

		val expected = """
			module 	variant${"   "}
			Summary	(total: 0)
		""".trimIndent().normalizeLineEndings()
		assertEquals(expected, result)
	}

	@Test fun `empty data (no counts) - expanded`() {
		val sut = TableGenerator(isPrintEmptyColumns = true, isPrintEmptyRows = true)

		val result = sut.build(NO_COUNTS)

		val expected = """
			module 	variant   	checker
			module1	variant1  	    N/A
			module2	variant2  	    N/A
			module3	variant3  	    N/A
			Summary	(total: 0)	    N/A
		""".trimIndent().normalizeLineEndings()
		assertEquals(expected, result)
	}

	@Test fun `empty data (no counts) - no rows`() {
		val sut = TableGenerator(isPrintEmptyColumns = true, isPrintEmptyRows = false)

		val result = sut.build(NO_COUNTS)

		val expected = """
			module 	variant   	checker
			Summary	(total: 0)	    N/A
		""".trimIndent().normalizeLineEndings()
		assertEquals(expected, result)
	}

	@Test fun `empty data (no counts) - no columns`() {
		val sut = TableGenerator(isPrintEmptyColumns = false, isPrintEmptyRows = true)

		val result = sut.build(NO_COUNTS)

		val expected = """
			module 	variant${"   "}
			module1	variant1${"  "}
			module2	variant2${"  "}
			module3	variant3${"  "}
			Summary	(total: 0)
		""".trimIndent().normalizeLineEndings()
		assertEquals(expected, result)
	}

	@Test fun `empty data (no counts) - collapsed`() {
		val sut = TableGenerator(isPrintEmptyColumns = false, isPrintEmptyRows = false)

		val result = sut.build(NO_COUNTS)

		val expected = """
			module 	variant${"   "}
			Summary	(total: 0)
		""".trimIndent().normalizeLineEndings()
		assertEquals(expected, result)
	}
}

private fun String.normalizeLineEndings(): String =
	this.replace(Regex("""\r?\n"""), System.lineSeparator())
