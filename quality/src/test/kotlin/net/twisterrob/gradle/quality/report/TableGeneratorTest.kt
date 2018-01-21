package net.twisterrob.gradle.quality.report

import org.junit.Assert.assertEquals
import org.junit.Test

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
		val result = TableGenerator().build(INPUT)

		val expected = """
			module 	variant  	checkstyle	pmd	unchecked
			module1	variant1 	         1	  2	      N/A
			module1	variant2 	       N/A	  3	      N/A
			module2	variant2 	       N/A	N/A	      N/A
			module2	variant3 	         0	N/A	      N/A
			module3	variant2 	       N/A	N/A	      N/A
			Summary	(total 6)	         1	  5	      N/A
		""".prepare()
		assertEquals(expected, result)
	}

	@Test fun doesNotPrintEmptyRows() {
		val result = TableGenerator(printEmptyRows = false).build(INPUT)

		val expected = """
			module 	variant  	checkstyle	pmd	unchecked
			module1	variant1 	         1	  2	      N/A
			module1	variant2 	       N/A	  3	      N/A
			module2	variant3 	         0	N/A	      N/A
			Summary	(total 6)	         1	  5	      N/A
		""".prepare()
		assertEquals(expected, result)
	}

	@Test fun doesNotPrintEmptyColumns() {
		val result = TableGenerator(printEmptyColumns = false).build(INPUT)

		val expected = """
			module 	variant  	checkstyle	pmd
			module1	variant1 	         1	  2
			module1	variant2 	       N/A	  3
			module2	variant2 	       N/A	N/A
			module2	variant3 	         0	N/A
			module3	variant2 	       N/A	N/A
			Summary	(total 6)	         1	  5
		""".prepare()
		assertEquals(expected, result)
	}

	@Test fun doesNotPrintEmptyRowsAndColumns() {
		val result = TableGenerator(printEmptyRows = false, printEmptyColumns = false).build(INPUT)

		val expected = """
			module 	variant  	checkstyle	pmd
			module1	variant1 	         1	  2
			module1	variant2 	       N/A	  3
			module2	variant3 	         0	N/A
			Summary	(total 6)	         1	  5
		""".prepare()
		assertEquals(expected, result)
	}
}

private fun String.prepare() = this.trimIndent().replace("\r?\n".toRegex(), System.lineSeparator())
