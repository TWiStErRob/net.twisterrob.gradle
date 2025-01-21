package net.twisterrob.gradle.quality.report

import com.flextrade.jfixture.JFixture
import io.mockk.every
import io.mockk.mockk
import net.twisterrob.gradle.common.grouper.Grouper
import net.twisterrob.gradle.quality.Violation
import net.twisterrob.gradle.quality.Violations
import net.twisterrob.gradle.quality.report.html.model.build
import net.twisterrob.gradle.quality.report.html.model.setField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.io.File

class ResultGeneratorTest {

	private val fixture = JFixture()

	@Test fun nothing() {
		val sut = ResultGenerator()

		val result = sut.build()

		assertNull(result)
	}

	@Test fun `no violations`() {
		val sut = ResultGenerator()

		val result = sut.build(
			fixture.build<Violations> {
				setField("violations", null)
			}
		)

		assertNull(result)
	}

	@Test fun `empty violations`() {
		val sut = ResultGenerator()

		val result = sut.build(
			fixture.build<Violations> {
				setField("violations", emptyList<Violation>())
			}
		)

		assertNull(result)
	}

	@Test fun single() {
		val sut = ResultGenerator()

		val result = sut.build(
			Violations(
				parser = "v-parser",
				module = "v-module",
				variant = "v-variant",
				report = File("path/to/v-report.html"),
				result = File("path/to/v-result.xml"),
				violations = listOf(
					Violation(
						rule = "rule",
						message = "message",
						category = "category",
						severity = Violation.Severity.ERROR,
						location = Violation.Location(
							module = Violation.Module(
								path = "path",
								name = "name",
								projectDir = File("projectDir"),
								rootDir = File("rootDir")
							),
							task = "task",
							variant = "variant",
							startLine = 1,
							endLine = 2,
							column = 3,
							file = File("path/to/file")
						),
						source = Violation.Source(
							parser = "parser",
							gatherer = "gatherer",
							reporter = "reporter",
							source = "source",
							report = File("path/to/report.html"),
							humanReport = File("path/to/humanReport.html")
						)
					),
				),
			),
		)

		val expected = """
			
			${ROOT}${PS}path${PS}to${PS}file:1 in path/variant
				reporter/rule
				message${EOL}
			
		""".trimIndent().fixLineEndings()
		assertEquals(expected, result)
	}

	@Test fun multiple() {
		val sut = ResultGenerator()

		val result = sut.build(
			Violations(
				parser = "v-parser",
				module = "v-module",
				variant = "v-variant",
				report = File("path/to/v-report.html"),
				result = File("path/to/v-result.xml"),
				violations = listOf(
					Violation(
						rule = "rule1",
						message = "message1",
						category = "category1",
						severity = Violation.Severity.ERROR,
						location = Violation.Location(
							module = Violation.Module(
								path = "path1",
								name = "name1",
								projectDir = File("projectDir1"),
								rootDir = File("rootDir1")
							),
							task = "task1",
							variant = "variant1",
							startLine = 1,
							endLine = 2,
							column = 3,
							file = File("path1/to1/file1")
						),
						source = Violation.Source(
							parser = "parser1",
							gatherer = "gatherer1",
							reporter = "reporter1",
							source = "source1",
							report = File("path1/to1/report1.html"),
							humanReport = File("path1/to1/humanReport1.html")
						)
					),
					Violation(
						rule = "rule2",
						message = "message2",
						category = "category2",
						severity = Violation.Severity.ERROR,
						location = Violation.Location(
							module = Violation.Module(
								path = "path2",
								name = "name2",
								projectDir = File("projectDir2"),
								rootDir = File("rootDir2")
							),
							task = "task2",
							variant = "variant2",
							startLine = 4,
							endLine = 5,
							column = 6,
							file = File("path2/to2/file2")
						),
						source = Violation.Source(
							parser = "parser2",
							gatherer = "gatherer2",
							reporter = "reporter2",
							source = "source2",
							report = File("path2/to2/report2.html"),
							humanReport = File("path2/to2/humanReport2.html")
						)
					),
				),
			),
		)

		val expected = """
			
			${ROOT}${PS}path1${PS}to1${PS}file1:1 in path1/variant1
				reporter1/rule1
				message1${EOL}
			${EOL}
			
			${ROOT}${PS}path2${PS}to2${PS}file2:4 in path2/variant2
				reporter2/rule2
				message2${EOL}
			
		""".trimIndent().fixLineEndings()
		assertEquals(expected, result)
	}


	@Test fun multiline() {
		val sut = ResultGenerator()

		val result = sut.build(
			Violations(
				parser = "v-parser",
				module = "v-module",
				variant = "v-variant",
				report = File("path/to/v-report.html"),
				result = File("path/to/v-result.xml"),
				violations = listOf(
					Violation(
						rule = "rule",
						message = """
							multi
								line
							message
							
							
							separated
						""".trimIndent(),
						category = "category",
						severity = Violation.Severity.ERROR,
						location = Violation.Location(
							module = Violation.Module(
								path = "path",
								name = "name",
								projectDir = File("projectDir"),
								rootDir = File("rootDir")
							),
							task = "task",
							variant = "variant",
							startLine = 1,
							endLine = 2,
							column = 3,
							file = File("path/to/file")
						),
						source = Violation.Source(
							parser = "parser",
							gatherer = "gatherer",
							reporter = "reporter",
							source = "source",
							report = File("path/to/report.html"),
							humanReport = File("path/to/humanReport.html")
						)
					),
				),
			),
		)

		val expected = """
			
			${ROOT}${PS}path${PS}to${PS}file:1 in path/variant
				reporter/rule
				multi
					line
				message
				separated${EOL}
			
		""".trimIndent().fixLineEndings()
		assertEquals(expected, result)
	}

	private companion object {
		private val PS = File.separator
		private val EOL_RAW = System.lineSeparator().replace("\n", "")
		/**
		 * [Triple quoted strings don't preserve system line endings](https://discuss.kotlinlang.org/t/26213)
		 * `String.trimIndent()` replaces line endings with \n.
		 *
		 * So using this as a stand-in for EOL, which is then replaced.
		 */
		private val EOL = System.lineSeparator().replace("\n", "").toByteArray().contentToString()
		private val ROOT = File(".").absoluteFile.parentFile
		private fun String.fixLineEndings(): String =
			this.replace(EOL, EOL_RAW)
	}
}

private fun ResultGenerator.build(vararg list: Violations): String? =
	this.build(
		mockk<Grouper.Start<Violations>> {
			every { this@mockk.list } returns list.toList()
		}
	)
