package net.twisterrob.gradle.quality.report.html

import com.flextrade.jfixture.JFixture
import net.twisterrob.gradle.quality.Violation
import net.twisterrob.gradle.quality.Violations
import net.twisterrob.gradle.quality.report.html.model.build
import net.twisterrob.gradle.test.RootProject
import net.twisterrob.gradle.test.createSubProject
import org.gradle.api.Project
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Duplicated to verify contents, but extracted to explain constant.
 * @see ALL_VARIANTS_NAME
 */
private const val ALL = "*"

@Suppress("UnnecessaryVariable")
class ViolationsDeduplicatorKtTest {
	private val fixtRoot = RootProject()
	private val fixture = JFixture()

	@Test fun `AGP7 lint vs checkstyle in module`() {
		val fixtModule = fixtRoot.createSubProject("module")
		val input = listOf(
			violations(fixtRoot, "debug", "lint"),
			violations(fixtRoot, "release", "lint"),
			violations(fixtModule, "debug", "lint"),
			violations(fixtModule, "release", "lint"),
			violations(fixtModule, ALL, "checkstyle", fixture.build()),
			violations(fixtModule, "debug", "checkstyle"),
			violations(fixtModule, "release", "checkstyle"),
		)
		val expected = input

		val actual = deduplicate(input)

		assertEquals(expected, actual)
	}

	@Test fun `AGP7 lint vs checkstyle in root`() {
		val input = listOf(
			violations(fixtRoot, "debug", "lint"),
			violations(fixtRoot, "release", "lint"),
			violations(fixtRoot, ALL, "checkstyle", fixture.build()),
			violations(fixtRoot, "debug", "checkstyle"),
			violations(fixtRoot, "release", "checkstyle"),
		)
		val expected = input

		val actual = deduplicate(input)

		assertEquals(expected, actual)
	}

	@Test fun `AGP4 lint vs checkstyle in module`() {
		val fixtModule = fixtRoot.createSubProject("module")
		val input = listOf(
			violations(fixtRoot, ALL, "lint"),
			violations(fixtRoot, "debug", "lint"),
			violations(fixtRoot, "release", "lint"),
			violations(fixtModule, ALL, "lint"),
			violations(fixtModule, "debug", "lint"),
			violations(fixtModule, "release", "lint"),
			violations(fixtModule, ALL, "checkstyle", fixture.build()),
			violations(fixtModule, "debug", "checkstyle"),
			violations(fixtModule, "release", "checkstyle"),
		)
		val expected = input

		val actual = deduplicate(input)

		assertEquals(expected, actual)
	}

	@Test fun `AGP4 lint vs checkstyle in root`() {
		val input = listOf(
			violations(fixtRoot, ALL, "lint"),
			violations(fixtRoot, "debug", "lint"),
			violations(fixtRoot, "release", "lint"),
			violations(fixtRoot, ALL, "checkstyle", fixture.build()),
			violations(fixtRoot, "debug", "checkstyle"),
			violations(fixtRoot, "release", "checkstyle"),
		)
		val expected = input

		val actual = deduplicate(input)

		assertEquals(expected, actual)
	}

	/**
	 * AGP4 and earlier shows the same violation for lint, lintDebug, lintRelease and lintVitalRelease.
	 */
	@Test fun `AGP4 duplicate lint`() {
		val violationToMerge: Violation = fixture.build()
		val results = listOf(
			violations(fixtRoot, ALL, "lint", violationToMerge),
			violations(fixtRoot, "debug", "lintVariant", violationToMerge),
			violations(fixtRoot, "release", "lintVariant", violationToMerge),
			violations(fixtRoot, "release", "lintVariant", violationToMerge),
		)
		val expected = listOf(
			violations(fixtRoot, ALL, "lint", violationToMerge),
			violations(fixtRoot, "debug", "lintVariant").noViolations(),
			violations(fixtRoot, "release", "lintVariant").noViolations(),
			violations(fixtRoot, "release", "lintVariant").noViolations(),
		)

		val actual = deduplicate(results)

		assertEquals(expected, actual)
	}

	@Test fun `duplicate with no 'all' present`() {
		val violationToMerge: Violation = fixture.build()
		val results = listOf(
			violations(fixtRoot, "debug", "lint", violationToMerge),
			violations(fixtRoot, "release", "lint", violationToMerge.copy()),
		)
		val expected = listOf(
			violations(fixtRoot, ALL, "lint", violationToMerge).unknownReports(),
			violations(fixtRoot, "debug", "lint").noViolations(),
			violations(fixtRoot, "release", "lint").noViolations(),
		)

		val actual = deduplicate(results)

		assertEquals(expected, actual)
	}

	@Test fun `duplicate with 'all' present but no violations`() {
		val violationToMerge: Violation = fixture.build()
		val results = listOf(
			violations(fixtRoot, ALL, "lint"),
			violations(fixtRoot, "debug", "lint", violationToMerge),
			violations(fixtRoot, "release", "lint", violationToMerge.copy()),
		)
		val expected = listOf(
			violations(fixtRoot, ALL, "lint", violationToMerge),
			violations(fixtRoot, "debug", "lint").noViolations(),
			violations(fixtRoot, "release", "lint").noViolations(),
		)

		val actual = deduplicate(results)

		assertEquals(expected, actual)
	}

	@Test fun `duplicate with 'all' present and has violations`() {
		val violationToMerge: Violation = fixture.build()
		val violationToKeep1: Violation = fixture.build()
		val violationToKeep2: Violation = fixture.build()
		val results = listOf(
			violations(fixtRoot, ALL, "lint", violationToKeep1, violationToKeep2),
			violations(fixtRoot, "debug", "lint", violationToMerge),
			violations(fixtRoot, "release", "lint", violationToMerge.copy()),
		)
		val expected = listOf(
			violations(fixtRoot, ALL, "lint", violationToKeep1, violationToKeep2, violationToMerge),
			violations(fixtRoot, "debug", "lint").noViolations(),
			violations(fixtRoot, "release", "lint").noViolations(),
		)

		val actual = deduplicate(results)

		assertEquals(expected, actual)
	}

	@Test fun `duplicate while keeping others intact`() {
		val violationToMerge: Violation = fixture.build()
		val violationToKeep1: Violation = fixture.build()
		val violationToKeep2: Violation = fixture.build()
		val violationUnrelated1: Violation = fixture.build()
		val violationUnrelated2: Violation = fixture.build()
		val results = listOf(
			violations(fixtRoot, "debug", "lint", violationToMerge, violationToKeep1),
			violations(fixtRoot, "release", "lint", violationToMerge.copy(), violationToKeep2),
			violations(fixtRoot, "debug", "checkstyle", violationUnrelated1),
			violations(fixtRoot, "release", "checkstyle", violationUnrelated2),
		)
		val expected = listOf(
			violations(fixtRoot, ALL, "lint", violationToMerge).unknownReports(),
			violations(fixtRoot, "debug", "lint", violationToKeep1),
			violations(fixtRoot, "release", "lint", violationToKeep2),
			violations(fixtRoot, "debug", "checkstyle", violationUnrelated1),
			violations(fixtRoot, "release", "checkstyle", violationUnrelated2),
		)

		val actual = deduplicate(results)

		assertEquals(expected, actual)
	}

	@Test fun `deduplicates multiple occurrences`() {
		val toMerge: Violation = fixture.build()
		val results = listOf(
			violations(fixtRoot, "debug", "lint", toMerge.copy(), toMerge.copy()),
			violations(fixtRoot, "release", "lint", toMerge.copy(), toMerge.copy(), toMerge.copy()),
		)
		val expected = listOf(
			violations(fixtRoot, ALL, "lint", toMerge).unknownReports(),
			violations(fixtRoot, "debug", "lint").noViolations(),
			violations(fixtRoot, "release", "lint").noViolations(),
		)

		val actual = deduplicate(results)

		assertEquals(expected, actual)
	}

	@Test fun `duplicate merging while keeping others intact`() {
		val violationInAll: Violation = fixture.build()
		val violationToMerge: Violation = fixture.build()
		val violationUnrelated: Violation = fixture.build()
		val results = listOf(
			violations(fixtRoot, ALL, "lint", violationInAll),
			violations(fixtRoot, "debug", "lint", violationToMerge),
			violations(fixtRoot, "release", "lint", violationToMerge.copy()),
			violations(fixtRoot, "debug", "checkstyle", violationUnrelated),
			violations(fixtRoot, "release", "checkstyle"),
		)
		val expected = listOf(
			violations(fixtRoot, ALL, "lint", violationInAll, violationToMerge),
			violations(fixtRoot, "debug", "lint").noViolations(),
			violations(fixtRoot, "release", "lint").noViolations(),
			violations(fixtRoot, "debug", "checkstyle", violationUnrelated),
			violations(fixtRoot, "release", "checkstyle"),
		)

		val actual = deduplicate(results)

		assertEquals(expected, actual)
	}

	@Test fun `violations from different modules don't get merged`() {
		val fixtModule = fixtRoot.createSubProject("module")
		val violationRootOnly: Violation = fixture.build()
		val violationModuleOnly: Violation = fixture.build()
		val violation: Violation = fixture.build()
		val results = listOf(
			violations(fixtRoot, "debug", "lint", violationRootOnly),
			violations(fixtRoot, "release", "lint", violationRootOnly, violation),
			violations(fixtModule, "debug", "lint", violationModuleOnly, violation),
			violations(fixtModule, "release", "lint", violationModuleOnly),
		)
		val expected = listOf(
			violations(fixtRoot, ALL, "lint", violationRootOnly).unknownReports(),
			violations(fixtRoot, "debug", "lint").noViolations(),
			violations(fixtRoot, "release", "lint", violation),
			violations(fixtModule, ALL, "lint", violationModuleOnly).unknownReports(),
			violations(fixtModule, "debug", "lint", violation),
			violations(fixtModule, "release", "lint").noViolations(),
		)

		val actual = deduplicate(results)

		assertEquals(expected, actual)
	}

	/**
	 * e.g. AGP 4.x debug, release, vitalRelease.
	 */
	@Test fun `deduplicate duplicate variants`() {
		val violationToMerge: Violation = fixture.build()
		val results = listOf(
			violations(fixtRoot, ALL, "lint", violationToMerge),
			violations(fixtRoot, "debug", "lint", violationToMerge),
			violations(fixtRoot, "release", "lint", violationToMerge),
			violations(fixtRoot, "release", "lint", violationToMerge),
		)
		val expected = listOf(
			violations(fixtRoot, ALL, "lint", violationToMerge),
			violations(fixtRoot, "debug", "lint").noViolations(),
			violations(fixtRoot, "release", "lint").noViolations(),
			violations(fixtRoot, "release", "lint").noViolations(),
		)

		val actual = deduplicate(results)

		assertEquals(expected, actual)
	}

	private fun assertEquals(
		expected: List<Violations>,
		actual: List<Violations>
	) {
		org.junit.jupiter.api.Assertions.assertEquals(
			expected.sortedBy { it.toString() }.joinToString(separator = "\n"),
			actual.sortedBy { it.toString() }.joinToString(separator = "\n")
		)
	}
}

private fun violations(
	project: Project,
	variant: String,
	type: String,
	vararg violations: Violation
): Violations {
	val suffix = if (variant == ALL) "-debug" else "-$variant"
	return Violations(
		module = project.path,
		parser = type,
		report = project.buildFile("reports/${type}${suffix}.html"),
		result = project.buildFile("reports/${type}${suffix}.xml"),
		variant = variant,
		violations = violations.toList().ifEmpty { null },
	)
}

private fun Project.buildFile(path: String): File =
	@Suppress("detekt.MaxChainedCallsOnSameLine")
	this.layout.buildDirectory.file(path).get().asFile

private fun Violations.noViolations(): Violations =
	Violations(
		parser = this.parser,
		module = this.module,
		variant = this.variant,
		result = this.result,
		report = this.report,
		violations = null,
	)

private fun Violations.unknownReports(): Violations =
	Violations(
		parser = this.parser,
		module = this.module,
		variant = this.variant,
		result = this.result,
		report = this.report,
		violations = this.violations?.toList(),
	)

private fun Violation.copy(): Violation =
	Violation(
		rule = this.rule,
		category = this.category,
		severity = this.severity,
		message = this.message,
		specifics = this.specifics,
		location = this.location,
		source = this.source,
	)
