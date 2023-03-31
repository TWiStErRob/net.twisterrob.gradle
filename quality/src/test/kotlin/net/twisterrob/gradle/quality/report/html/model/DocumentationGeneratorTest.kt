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
	fun `lint generates the check documentation link`(rule: String, expected: URI) {
		val fixtViolation: Violation = fixture.build {
			source.setField("reporter", "ANDROIDLINT")
			setField("rule", rule)
		}

		val docUri = sut.getDocumentationUrl(fixtViolation)

		assertEquals(expected, docUri)
	}

	@ParameterizedTest
	@CsvSource(
		value = [
			"style,        ForbiddenVoid, https://detekt.dev/docs/rules/style#forbiddenvoid",
			"empty-blocks, EmptyIfBlock,  https://detekt.dev/docs/rules/empty-blocks#emptyifblock",
			"formatting,   Wrapping,      https://detekt.dev/docs/rules/formatting#wrapping",
			"null,         MagicConstant, null",
		],
		nullValues = ["null"],
	)
	fun `detekt generates the rule documentation link`(category: String?, rule: String, expected: URI?) {
		val fixtViolation: Violation = fixture.build {
			source.setField("reporter", "DETEKT")
			setField("category", category)
			setField("rule", rule)
		}

		val docUri = sut.getDocumentationUrl(fixtViolation)

		assertEquals(expected, docUri)
	}

	@ParameterizedTest
	@CsvSource(
		value = [
			"imports, UnusedImportsCheck,   https://checkstyle.sourceforge.io/config_imports.html#UnusedImports",
			"metrics, JavaNCSSCheck,        https://checkstyle.sourceforge.io/config_metrics.html#JavaNCSS",
			"metrics, NPathComplexityCheck, https://checkstyle.sourceforge.io/config_metrics.html#NPathComplexity",
			"null,    UnknownCheck,         null",
		],
		nullValues = ["null"],
	)
	fun `checkstyle generates the check documentation link`(category: String?, check: String, expected: URI?) {
		val fixtViolation: Violation = fixture.build {
			source.setField("reporter", "CHECKSTYLE")
			setField("category", category)
			setField("rule", check)
		}

		val docUri = sut.getDocumentationUrl(fixtViolation)

		assertEquals(expected, docUri)
	}

	@ParameterizedTest
	@CsvSource(
		value = [
			"Code Style,  NoPackage,        https://pmd.github.io/latest/pmd_rules_java_codestyle.html#nopackage",
			"Design,      GodClass,         https://pmd.github.io/latest/pmd_rules_java_design.html#godclass",
			"Error Prone, AvoidCatchingNPE, https://pmd.github.io/latest/pmd_rules_java_errorprone.html#avoidcatchingnpe",
			"Custom,      SomethingCustom,  null",
			"null,        UnknownRule,      null",
		],
		nullValues = ["null"],
	)
	fun `pmd generates the rule documentation link`(category: String?, check: String, expected: URI?) {
		val fixtViolation: Violation = fixture.build {
			source.setField("reporter", "PMD")
			setField("category", category)
			setField("rule", check)
		}

		val docUri = sut.getDocumentationUrl(fixtViolation)

		assertEquals(expected, docUri)
	}

	@Test
	fun `pmd returns the rule documentation link from the message`() {
		val fixtViolation: Violation = fixture.build {
			source.setField("reporter", "PMD")
			setField("category", "Does Not Matter")
			setField("rule", "DoesNotMatter")
			setField(
				"message",
				// Two tabs and newlines are from realistic data.
				"""
					Avoid printStackTrace(); use a logger call instead.
					${"\t\t"}
					
					Best Practices https://pmd.github.io/pmd-6.39.0/pmd_rules_java_bestpractices.html#avoidprintstacktrace
				""".trimIndent()
			)
		}
		val expected =
			URI.create("https://pmd.github.io/pmd-6.39.0/pmd_rules_java_bestpractices.html#avoidprintstacktrace")

		val docUri = sut.getDocumentationUrl(fixtViolation)

		assertEquals(expected, docUri)
	}
}
