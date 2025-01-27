package net.twisterrob.gradle.quality.report.html.model

import com.flextrade.jfixture.JFixture
import net.twisterrob.gradle.quality.Violation
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class SuppressionGeneratorTest {

	private val fixture = createAndroidLintFixture()

	private val sut = SuppressionGenerator()

	@Nested
	inner class `Android Lint suppressions` {

		@Test
		fun `java files are suppressed with annotation`() {
			val input = fixture.build<Violation>().apply {
				this.location.setField("file", File(fixture.build<String>() + ".java"))
			}
			@Language("java")
			val expected = """
				@SuppressLint("${input.rule}") // TODO Explanation.
			""".trimIndent()

			val output = sut.getSuppression(input)

			assertEquals(expected, output)
		}

		@Test
		fun `kotlin files are suppressed with annotation`() {
			val input = fixture.build<Violation>().apply {
				this.location.setField("file", File(fixture.build<String>() + ".kt"))
			}
			@Language("kotlin")
			val expected = """
				@SuppressLint("${input.rule}") // TODO Explanation.
			""".trimIndent()

			val output = sut.getSuppression(input)

			assertEquals(expected, output)
		}

		@Test
		fun `xml files are suppressed with attribute`() {
			val input = fixture.build<Violation>().apply {
				this.location.setField("file", File(fixture.build<String>() + ".xml"))
			}
			@Language("XML")
			val expected = """
				tools:ignore="${input.rule}"
			""".trimIndent()

			val output = sut.getSuppression(input)

			assertEquals(expected, output)
		}

		@Test
		fun `unknown nested files are suppressed with lint-xml`() {
			val input = fixture.build<Violation>().apply {
				location.setField("file", location.module.projectDir.resolve("some/relative/file"))
			}
			@Language("XML")
			val expected = """
				<issue id="${input.rule}">
				    <!-- TODO Explanation. -->
				    <ignore path="some/relative/file" />
				</issue>
			""".trimIndent()

			val output = sut.getSuppression(input)

			assertEquals(expected, output)
		}

		@Test
		fun `unknown external directories are suppressed with lint-xml`(@TempDir temp: File) {
			val tempDir = temp.resolve("some/dir").apply { assertTrue(mkdirs()) }
			val input = fixture.build<Violation>().apply {
				location.setField("file", tempDir)
			}
			@Language("XML")
			val expected = """
				<issue id="${input.rule}">
				    <!-- TODO Explanation. -->
				    <ignore path="dir" />
				</issue>
			""".trimIndent()

			val output = sut.getSuppression(input)

			assertEquals(expected, output)
		}

		@Test
		fun `unknown external files are suppressed with lint-xml`(@TempDir temp: File) {
			val input = fixture.build<Violation>().apply {
				location.setField("file", temp.resolve("some/file.name"))
			}
			@Language("XML")
			val expected = """
				<issue id="${input.rule}">
				    <!-- TODO Explanation. -->
				    <ignore path="file.name" />
				</issue>
			""".trimIndent()

			val output = sut.getSuppression(input)

			assertEquals(expected, output)
		}
	}

	companion object {

		@Suppress("detekt.NestedScopeFunctions")
		private fun createAndroidLintFixture(): JFixture =
			JFixture().apply {
				customise().intercept(Violation::class.java) { violation ->
					violation.source.setField("reporter", "ANDROIDLINT")
					violation.location.module.apply {
						setField("projectDir", rootDir.resolve(projectDir))
					}
				}
			}
	}
}
