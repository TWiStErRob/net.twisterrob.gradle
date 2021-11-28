package net.twisterrob.gradle.quality.report.html

import com.flextrade.jfixture.JFixture
import net.twisterrob.gradle.common.ALL_VARIANTS_NAME
import net.twisterrob.gradle.quality.Violation
import net.twisterrob.gradle.quality.Violations
import net.twisterrob.gradle.quality.report.html.model.build
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@Suppress("UnnecessaryVariable")
class ViolationsDeduplicatorKtTest {
	private val fixtRoot = ProjectBuilder.builder().build()
	private val fixture = JFixture().apply {
		customise().sameInstance(Project::class.java, fixtRoot)
		customise().lazyInstance(Task::class.java) {
			fixtRoot.tasks.create(build(), DefaultTask::class.java)
		}
	}

	@Test fun `AGP7 lint vs checkstyle in module`() {
		val fixtModule = ProjectBuilder.builder().withName("module").withParent(fixtRoot).build()
		val input = listOf(
			violations(fixtRoot, "debug", "lint"),
			violations(fixtRoot, "release", "lint"),
			violations(fixtModule, "debug", "lint"),
			violations(fixtModule, "release", "lint"),
			violations(fixtModule, "*", "checkstyle", fixture.build()),
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
			violations(fixtRoot, "*", "checkstyle", fixture.build()),
			violations(fixtRoot, "debug", "checkstyle"),
			violations(fixtRoot, "release", "checkstyle"),
		)
		val expected = input

		val actual = deduplicate(input)

		assertEquals(expected, actual)
	}

	@Test fun `AGP4 lint vs checkstyle in module`() {
		val fixtModule = ProjectBuilder.builder().withName("module").withParent(fixtRoot).build()
		val input = listOf(
			violations(fixtRoot, "*", "lint"),
			violations(fixtRoot, "debug", "lint"),
			violations(fixtRoot, "release", "lint"),
			violations(fixtModule, "*", "lint"),
			violations(fixtModule, "debug", "lint"),
			violations(fixtModule, "release", "lint"),
			violations(fixtModule, "*", "checkstyle", fixture.build()),
			violations(fixtModule, "debug", "checkstyle"),
			violations(fixtModule, "release", "checkstyle"),
		)
		val expected = input

		val actual = deduplicate(input)

		assertEquals(expected, actual)
	}

	@Test fun `AGP4 lint vs checkstyle in root`() {
		val input = listOf(
			violations(fixtRoot, "*", "lint"),
			violations(fixtRoot, "debug", "lint"),
			violations(fixtRoot, "release", "lint"),
			violations(fixtRoot, "*", "checkstyle", fixture.build()),
			violations(fixtRoot, "debug", "checkstyle"),
			violations(fixtRoot, "release", "checkstyle"),
		)
		val expected = input

		val actual = deduplicate(input)

		assertEquals(expected, actual)
	}

	@Test fun `AGP4 duplicate lint`() {
		val violation = fixture.build<Violation>()
		val results = listOf(
			violations(fixtRoot, "*", "lint", violation),
			violations(fixtRoot, "debug", "lint", violation),
			violations(fixtRoot, "release", "lint", violation),
		)
		val expected = listOf(
			violations(fixtRoot, "*", "lint", violation),
			violations(fixtRoot, "debug", "lint"),
			violations(fixtRoot, "release", "lint"),
		)

		val actual = deduplicate(results)

		assertEquals(expected, actual)
	}

	private fun assertEquals(
		expected: List<Violations>,
		actual: List<Violations>
	) {
		assertEquals(
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
	val suffix = if (variant == ALL_VARIANTS_NAME) "" else "-$variant"
	return Violations(
		module = project.path,
		parser = type,
		report = project.buildDir.resolve("""reports\$type$suffix.html"""),
		result = project.buildDir.resolve("""reports\$type$suffix.xml"""),
		variant = variant,
		violations = violations.toList().ifEmpty { null },
	)
}
