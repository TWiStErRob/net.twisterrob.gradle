package net.twisterrob.gradle.quality.report

import org.junit.Test

import static org.junit.Assert.assertEquals

class TableGeneratorTest {

	static final LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, Integer>>> INPUT = [
			'module1': [
					'variant1': [
							'checkstyle': 1,
							'pmd'       : 2,
					],
					'variant2': [
							'pmd': 3,
					],
			],
			'module2': [
					'variant2': [ : ],
					'variant3': [
							'checkstyle': 0,
							'unchecked': null,
					],
			],
			'module3': [
					'variant2' : [ : ],
			],
	]

	@Test void printSummary() {
		def result = new TableGenerator().build(INPUT)

		def expected = """\
			module \tvariant  \tcheckstyle\tpmd\tunchecked
			module1\tvariant1 \t         1\t  2\t      N/A
			module1\tvariant2 \t       N/A\t  3\t      N/A
			module2\tvariant2 \t       N/A\tN/A\t      N/A
			module2\tvariant3 \t         0\tN/A\t      N/A
			module3\tvariant2 \t       N/A\tN/A\t      N/A
			Summary\t(total 6)\t         1\t  5\t      N/A""".stripIndent().replaceAll(/\r?\n/, System.lineSeparator())
		assertEquals(expected, result)
	}

	@Test void doesNotPrintEmptyRows() {
		def result = new TableGenerator(printEmptyRows: false).build(INPUT)

		def expected = """\
			module \tvariant  \tcheckstyle\tpmd\tunchecked
			module1\tvariant1 \t         1\t  2\t      N/A
			module1\tvariant2 \t       N/A\t  3\t      N/A
			module2\tvariant3 \t         0\tN/A\t      N/A
			Summary\t(total 6)\t         1\t  5\t      N/A""".stripIndent().replaceAll(/\r?\n/, System.lineSeparator())
		assertEquals(expected, result)
	}

	@Test void doesNotPrintEmptyColumns() {
		def result = new TableGenerator(printEmptyColumns: false).build(INPUT)

		def expected = """\
			module \tvariant  \tcheckstyle\tpmd
			module1\tvariant1 \t         1\t  2
			module1\tvariant2 \t       N/A\t  3
			module2\tvariant2 \t       N/A\tN/A
			module2\tvariant3 \t         0\tN/A
			module3\tvariant2 \t       N/A\tN/A
			Summary\t(total 6)\t         1\t  5""".stripIndent().replaceAll(/\r?\n/, System.lineSeparator())
		assertEquals(expected, result)
	}

	@Test void doesNotPrintEmptyRowsAndColumns() {
		def result = new TableGenerator(printEmptyRows: false, printEmptyColumns: false).build(INPUT)

		def expected = """\
			module \tvariant  \tcheckstyle\tpmd
			module1\tvariant1 \t         1\t  2
			module1\tvariant2 \t       N/A\t  3
			module2\tvariant3 \t         0\tN/A
			Summary\t(total 6)\t         1\t  5""".stripIndent().replaceAll(/\r?\n/, System.lineSeparator())
		assertEquals(expected, result)
	}
}
