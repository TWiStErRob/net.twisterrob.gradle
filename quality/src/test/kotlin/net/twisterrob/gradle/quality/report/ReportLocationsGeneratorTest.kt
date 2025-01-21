package net.twisterrob.gradle.quality.report

import com.flextrade.jfixture.JFixture
import io.mockk.every
import io.mockk.mockk
import net.twisterrob.gradle.common.grouper.Grouper
import net.twisterrob.gradle.quality.Violations
import net.twisterrob.gradle.quality.report.html.model.build
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.io.File

class ReportLocationsGeneratorTest {

	private val fixture = JFixture()

	@Test fun nothing() {
		val sut = ReportLocationsGenerator()

		val result = sut.build()

		assertNull(result)
	}

	@Test fun single() {
		val sut = ReportLocationsGenerator()

		val result = sut.build(
			Violations(
				parser = "parser",
				module = "module",
				variant = "variant",
				report = File("path/to/report.html"),
				result = File("path/to/result.xml"),
				violations = listOf(fixture.build(), fixture.build()),
			),
		)

		val expected = """
			module:parser@variant (2): path${PS}to${PS}report.html
			
		""".trimIndent().normalizeLineEndings()
		assertEquals(expected, result)
	}

	@Test fun `filter empty and null`() {
		val sut = ReportLocationsGenerator()

		val result = sut.build(
			Violations(
				parser = "parser1",
				module = "module1",
				variant = "variant1",
				report = File("path/to/report1.html"),
				result = File("path/to/result1.xml"),
				violations = listOf(fixture.build(), fixture.build(), fixture.build()),
			),
			Violations(
				parser = "parser2",
				module = "module2",
				variant = "variant2",
				report = File("path/to/report2.html"),
				result = File("path/to/result2.xml"),
				violations = emptyList(),
			),
			Violations(
				parser = "parser3",
				module = "module3",
				variant = "variant3",
				report = File("path/to/report3.html"),
				result = File("path/to/result3.xml"),
				violations = listOf(fixture.build()),
			),
			Violations(
				parser = "parser4",
				module = "module4",
				variant = "variant4",
				report = File("path/to/report4.html"),
				result = File("path/to/result4.xml"),
				violations = null,
			),
			Violations(
				parser = "parser5",
				module = "module5",
				variant = "variant5",
				report = File("path/to/report5.html"),
				result = File("path/to/result5.xml"),
				violations = listOf(fixture.build(), fixture.build()),
			),
		)

		val expected = """
			module1:parser1@variant1 (3): path${PS}to${PS}report1.html
			module3:parser3@variant3 (1): path${PS}to${PS}report3.html
			module5:parser5@variant5 (2): path${PS}to${PS}report5.html
			
		""".trimIndent().normalizeLineEndings()
		assertEquals(expected, result)
	}

	private companion object {
		private val PS = File.separator
	}
}

private fun String.normalizeLineEndings(): String =
	this.replace(Regex("""\r?\n"""), System.lineSeparator())

private fun ReportLocationsGenerator.build(vararg list: Violations): String? =
	this.build(
		mockk<Grouper.Start<Violations>> {
			every { this@mockk.list } returns list.toList()
		}
	)
