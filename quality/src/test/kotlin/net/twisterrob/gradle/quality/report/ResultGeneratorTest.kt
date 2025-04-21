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
					violation(0),
				),
			),
		)

		val expected = """
			
			${ROOT}${PS}path0${PS}to0${PS}file0:1 in path0/variant0
				reporter0/rule0
				message0${EOL}
			
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
					violation(1),
					violation(2),
				),
			),
		)

		val expected = """
			
			${ROOT}${PS}path1${PS}to1${PS}file1:4 in path1/variant1
				reporter1/rule1
				message1${EOL}
			${EOL}
			
			${ROOT}${PS}path2${PS}to2${PS}file2:7 in path2/variant2
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
					violation(
						0,
						message = """
							multi
								line
							message
							
							
							separated
						""".trimIndent(),
					)
				),
			),
		)

		val expected = """
			
			${ROOT}${PS}path0${PS}to0${PS}file0:1 in path0/variant0
				reporter0/rule0
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

private fun violation(i: Int, message: String = "message${i}"): Violation =
	Violation(
		rule = "rule${i}",
		message = message,
		category = "category${i}",
		severity = Violation.Severity.ERROR,
		location = Violation.Location(
			module = Violation.Module(
				path = "path${i}",
				name = "name${i}",
				projectDir = File("projectDir${i}"),
				rootDir = File("rootDir${i}")
			),
			task = "task${i}",
			variant = "variant${i}",
			startLine = 3 * i + 1,
			endLine = 3 * i + 2,
			column = 3 * i + 3,
			file = File("path${i}/to${i}/file${i}")
		),
		source = Violation.Source(
			parser = "parser${i}",
			gatherer = "gatherer${i}",
			reporter = "reporter${i}",
			source = "source${i}",
			report = File("path${i}/to${i}/report${i}.html"),
			humanReport = File("path${i}/to${i}/humanReport${i}.html")
		)
	)
