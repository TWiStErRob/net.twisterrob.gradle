package net.twisterrob.gradle.internal.deprecation

import org.gradle.util.GradleVersion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class NextMajorVersionTest {

	@CsvSource(
		"6.3-milestone-1, 7.0",
		"6.3, 7.0",
		"6.3.1, 7.0",
		"6.4, 7.0",
		"6.12, 7.0",
		"7.0, 8.0",
		"7.0-rc-2, 8.0",
		"7.0.1, 8.0",
		"7.1, 8.0",
		"8.0, 9.0",
		"8.14.3, 9.0",
		"9.0, 10.0",
		"9.1-rc-3, 10.0",
	)
	@ParameterizedTest
	fun `nextMajorVersion returns next major dot 0`(input: String, expected: String) {
		val inputVersion = GradleVersion.version(input)
		val expectedVersion = GradleVersion.version(expected)

		val nextMajor = nextMajorVersion(inputVersion)

		assertEquals(expectedVersion, nextMajor)
	}

	@CsvSource(
		"6.3-milestone-1, 7",
		"6.3, 7",
		"6.3.1, 7",
		"6.4, 7",
		"6.12, 7",
		"7.0, 8",
		"7.0-rc-2, 8",
		"7.0.1, 8",
		"7.1, 8",
		"8.0, 9",
		"8.14.3, 9",
		"9.0, 10",
		"9.1-rc-3, 10",
	)
	@ParameterizedTest
	fun `nextMajorVersionNumber returns next`(input: String, expected: Int) {
		val inputVersion = GradleVersion.version(input)

		val nextMajor = nextMajorVersionNumber(inputVersion)

		assertEquals(expected, nextMajor)
	}

	@CsvSource(
		"6.3-milestone-1, Gradle 7.0",
		"6.3, Gradle 7.0",
		"6.3.1, Gradle 7.0",
		"6.4, Gradle 7.0",
		"6.12, Gradle 7.0",
		"7.0, Gradle 8.0",
		"7.0-rc-2, Gradle 8.0",
		"7.0.1, Gradle 8.0",
		"7.1, Gradle 8.0",
		"8.0, Gradle 9.0",
		"8.14.3, Gradle 9.0",
		"9.0, Gradle 10",
		"9.1-rc-3, Gradle 10",
	)
	@ParameterizedTest
	fun `nextMajorVersionStringForDeprecation handles Gradle 9 dropping dot 0`(input: String, expected: String) {
		val inputVersion = GradleVersion.version(input)

		val nextMajor = nextMajorVersionStringForDeprecation(inputVersion)

		assertEquals(expected, nextMajor)
	}

	@CsvSource(
		"4.12.3",
		"5.0",
		"5.10",
		"6.0",
		"6.2",
		"6.2.3-milestone-1",
		"6.2.3-rc-3",
	)
	@ParameterizedTest
	fun `nextMajorVersion family fails with old versions`(input: String) {
		val inputVersion = GradleVersion.version(input)

		assertThrows<IllegalArgumentException> { nextMajorVersion(inputVersion) }
		assertThrows<IllegalArgumentException> { nextMajorVersionNumber(inputVersion) }
		assertThrows<IllegalArgumentException> { nextMajorVersionStringForDeprecation(inputVersion) }
	}
}
