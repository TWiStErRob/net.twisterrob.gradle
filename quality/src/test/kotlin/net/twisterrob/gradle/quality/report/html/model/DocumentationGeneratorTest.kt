package net.twisterrob.gradle.quality.report.html.model

import com.flextrade.jfixture.JFixture
import net.twisterrob.gradle.quality.Violation
import org.gradle.api.Project
import org.gradle.api.Task
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.mock
import java.net.URI
import kotlin.test.assertEquals

class DocumentationGeneratorTest {

	private val sut = DocumentationGenerator()

	private val fixture = JFixture().apply {
		customise().lazyInstance(Project::class.java) { mock() }
		customise().lazyInstance(Task::class.java) { mock() }
	}

	@Test
	fun `unknown reporter gives no documentation`() {
		val fixtViolation: Violation = fixture.build {
			source.setField("reporter", "UNKNOWN")
		}

		val docUri = sut.getDocumentationUrl(fixtViolation)

		assertEquals(null, docUri)
	}

	@ParameterizedTest
	@CsvSource(
		value = [
			"Slices,       https://googlesamples.github.io/android-custom-lint-rules/checks/Slices.md.html",
			"LabelFor,     https://googlesamples.github.io/android-custom-lint-rules/checks/LabelFor.md.html",
			"UseAlpha2,    https://googlesamples.github.io/android-custom-lint-rules/checks/UseAlpha2.md.html",
			"EnforceUTF8,  https://googlesamples.github.io/android-custom-lint-rules/checks/EnforceUTF8.md.html",
			"SQLiteString, https://googlesamples.github.io/android-custom-lint-rules/checks/SQLiteString.md.html",
		]
	)
	fun `lint returns the check documentation link`(rule: String, expected: URI) {
		val fixtViolation: Violation = fixture.build {
			source.setField("reporter", "ANDROIDLINT")
			setField("rule", rule)
		}

		val docUri = sut.getDocumentationUrl(fixtViolation)

		assertEquals(expected, docUri)
	}
}
