package net.twisterrob.gradle.quality.report

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TableGeneratorTest {

	companion object {
		val INPUT = mapOf(
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
				"variant2" to mapOf(
				),
				"variant3" to mapOf(
					"checkstyle" to 0,
					"unchecked" to null
				)
			),
			"module3" to mapOf(
				"variant2" to mapOf(
				)
			)
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
			Summary	(total: 6)	         1	  5	      N/A
		""".prepare()
		assertEquals(expected, result)
	}

	@Test fun doesNotPrintEmptyRows() {
		val sut = TableGenerator(printEmptyRows = false)

		val result = sut.build(INPUT)

		val expected = """
			module 	variant  	checkstyle	pmd	unchecked
			module1	variant1 	         1	  2	      N/A
			module1	variant2 	       N/A	  3	      N/A
			module2	variant3 	         0	N/A	      N/A
			Summary	(total: 6)	         1	  5	      N/A
		""".prepare()
		assertEquals(expected, result)
	}

	@Test fun doesNotPrintEmptyColumns() {
		val sut = TableGenerator(printEmptyColumns = false)

		val result = sut.build(INPUT)

		val expected = """
			module 	variant  	checkstyle	pmd
			module1	variant1 	         1	  2
			module1	variant2 	       N/A	  3
			module2	variant2 	       N/A	N/A
			module2	variant3 	         0	N/A
			module3	variant2 	       N/A	N/A
			Summary	(total: 6)	         1	  5
		""".prepare()
		assertEquals(expected, result)
	}

	@Test fun doesNotPrintEmptyRowsAndColumns() {
		val sut = TableGenerator(printEmptyRows = false, printEmptyColumns = false)

		val result = sut.build(INPUT)

		val expected = """
			module 	variant  	checkstyle	pmd
			module1	variant1 	         1	  2
			module1	variant2 	       N/A	  3
			module2	variant3 	         0	N/A
			Summary	(total: 6)	         1	  5
		""".prepare()
		assertEquals(expected, result)
	}

	@Test fun `empty data (no modules)`() {
		val sut = TableGenerator()

		val result = sut.build(emptyMap())

		val expected = """
			module 	variant${"   "}
			Summary	(total: 0)
		""".prepare()
		assertEquals(expected, result)
	}

	@Test fun `empty data (no variants)`() {
		val sut = TableGenerator()

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
		""".prepare()
		assertEquals(expected, result)
	}

	@Test fun `empty data (no parsers)`() {
		val sut = TableGenerator()

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
		""".prepare()
		assertEquals(expected, result)
	}
}

private fun String.prepare(): String =
	this
		.trimIndent()
		.replace(Regex("""\r?\n"""), System.lineSeparator())
