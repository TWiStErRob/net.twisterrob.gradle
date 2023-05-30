package net.twisterrob.gradle.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

class KotlinVersionsTest {

	companion object {
		/**
		 * List of AGP versions from https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-gradle-plugin.
		 */
		@Suppress("LongMethod")
		@JvmStatic
		fun mavenCentral(): List<Arguments> =
			listOf(
				Arguments.of("1.9.0-Beta", KotlinVersion(major = 1, minor = 9, patch = 0)),
				Arguments.of("1.8.21", KotlinVersion(major = 1, minor = 8, patch = 21)),
				Arguments.of("1.8.20", KotlinVersion(major = 1, minor = 8, patch = 20)),
				Arguments.of("1.8.20-RC2", KotlinVersion(major = 1, minor = 8, patch = 20)),
				Arguments.of("1.8.20-RC", KotlinVersion(major = 1, minor = 8, patch = 20)),
				Arguments.of("1.8.20-Beta", KotlinVersion(major = 1, minor = 8, patch = 20)),
				Arguments.of("1.8.10", KotlinVersion(major = 1, minor = 8, patch = 10)),
				Arguments.of("1.8.0", KotlinVersion(major = 1, minor = 8, patch = 0)),
				Arguments.of("1.8.0-RC2", KotlinVersion(major = 1, minor = 8, patch = 0)),
				Arguments.of("1.8.0-RC", KotlinVersion(major = 1, minor = 8, patch = 0)),
				Arguments.of("1.8.0-Beta", KotlinVersion(major = 1, minor = 8, patch = 0)),
				Arguments.of("1.7.22", KotlinVersion(major = 1, minor = 7, patch = 22)),
				Arguments.of("1.7.21", KotlinVersion(major = 1, minor = 7, patch = 21)),
				Arguments.of("1.7.20", KotlinVersion(major = 1, minor = 7, patch = 20)),
				Arguments.of("1.7.20-RC", KotlinVersion(major = 1, minor = 7, patch = 20)),
				Arguments.of("1.7.20-Beta", KotlinVersion(major = 1, minor = 7, patch = 20)),
				Arguments.of("1.7.10", KotlinVersion(major = 1, minor = 7, patch = 10)),
				Arguments.of("1.7.0", KotlinVersion(major = 1, minor = 7, patch = 0)),
				Arguments.of("1.7.0-RC2", KotlinVersion(major = 1, minor = 7, patch = 0)),
				Arguments.of("1.7.0-RC", KotlinVersion(major = 1, minor = 7, patch = 0)),
				Arguments.of("1.7.0-Beta", KotlinVersion(major = 1, minor = 7, patch = 0)),
				Arguments.of("1.6.21", KotlinVersion(major = 1, minor = 6, patch = 21)),
				Arguments.of("1.6.20", KotlinVersion(major = 1, minor = 6, patch = 20)),
				Arguments.of("1.6.20-RC2", KotlinVersion(major = 1, minor = 6, patch = 20)),
				Arguments.of("1.6.20-RC", KotlinVersion(major = 1, minor = 6, patch = 20)),
				Arguments.of("1.6.20-M1", KotlinVersion(major = 1, minor = 6, patch = 20)),
				Arguments.of("1.6.10", KotlinVersion(major = 1, minor = 6, patch = 10)),
				Arguments.of("1.6.10-RC", KotlinVersion(major = 1, minor = 6, patch = 10)),
				Arguments.of("1.6.0", KotlinVersion(major = 1, minor = 6, patch = 0)),
				Arguments.of("1.6.0-RC2", KotlinVersion(major = 1, minor = 6, patch = 0)),
				Arguments.of("1.6.0-RC", KotlinVersion(major = 1, minor = 6, patch = 0)),
				Arguments.of("1.6.0-M1", KotlinVersion(major = 1, minor = 6, patch = 0)),
				Arguments.of("1.5.32", KotlinVersion(major = 1, minor = 5, patch = 32)),
				Arguments.of("1.5.31", KotlinVersion(major = 1, minor = 5, patch = 31)),
				Arguments.of("1.5.30", KotlinVersion(major = 1, minor = 5, patch = 30)),
				Arguments.of("1.5.30-RC", KotlinVersion(major = 1, minor = 5, patch = 30)),
				Arguments.of("1.5.30-M1", KotlinVersion(major = 1, minor = 5, patch = 30)),
				Arguments.of("1.5.21", KotlinVersion(major = 1, minor = 5, patch = 21)),
				Arguments.of("1.5.20", KotlinVersion(major = 1, minor = 5, patch = 20)),
				Arguments.of("1.5.20-RC", KotlinVersion(major = 1, minor = 5, patch = 20)),
				Arguments.of("1.5.20-M1", KotlinVersion(major = 1, minor = 5, patch = 20)),
				Arguments.of("1.5.10", KotlinVersion(major = 1, minor = 5, patch = 10)),
				Arguments.of("1.5.0", KotlinVersion(major = 1, minor = 5, patch = 0)),
				Arguments.of("1.5.0-RC", KotlinVersion(major = 1, minor = 5, patch = 0)),
				Arguments.of("1.5.0-M2", KotlinVersion(major = 1, minor = 5, patch = 0)),
				Arguments.of("1.5.0-M1", KotlinVersion(major = 1, minor = 5, patch = 0)),
				Arguments.of("1.4.32", KotlinVersion(major = 1, minor = 4, patch = 32)),
				Arguments.of("1.4.31", KotlinVersion(major = 1, minor = 4, patch = 31)),
				Arguments.of("1.4.30", KotlinVersion(major = 1, minor = 4, patch = 30)),
				Arguments.of("1.4.30-RC", KotlinVersion(major = 1, minor = 4, patch = 30)),
				Arguments.of("1.4.30-M1", KotlinVersion(major = 1, minor = 4, patch = 30)),
				//Arguments.of("1.4.21-2", KotlinVersion(major = 1, minor = 4, patch = 21)),
				Arguments.of("1.4.21", KotlinVersion(major = 1, minor = 4, patch = 21)),
				Arguments.of("1.4.20", KotlinVersion(major = 1, minor = 4, patch = 20)),
				Arguments.of("1.4.20-RC", KotlinVersion(major = 1, minor = 4, patch = 20)),
				Arguments.of("1.4.20-M2", KotlinVersion(major = 1, minor = 4, patch = 20)),
				Arguments.of("1.4.20-M1", KotlinVersion(major = 1, minor = 4, patch = 20)),
				Arguments.of("1.4.10", KotlinVersion(major = 1, minor = 4, patch = 10)),
				Arguments.of("1.4.0", KotlinVersion(major = 1, minor = 4, patch = 0)),
				//Arguments.of("1.4.0-rc", KotlinVersion(major = 1, minor = 4, patch = 0)),
				Arguments.of("1.3.72", KotlinVersion(major = 1, minor = 3, patch = 72)),
				Arguments.of("1.3.71", KotlinVersion(major = 1, minor = 3, patch = 71)),
				Arguments.of("1.3.70", KotlinVersion(major = 1, minor = 3, patch = 70)),
				Arguments.of("1.3.61", KotlinVersion(major = 1, minor = 3, patch = 61)),
				Arguments.of("1.3.60", KotlinVersion(major = 1, minor = 3, patch = 60)),
				Arguments.of("1.3.50", KotlinVersion(major = 1, minor = 3, patch = 50)),
				Arguments.of("1.3.41", KotlinVersion(major = 1, minor = 3, patch = 41)),
				Arguments.of("1.3.40", KotlinVersion(major = 1, minor = 3, patch = 40)),
				Arguments.of("1.3.31", KotlinVersion(major = 1, minor = 3, patch = 31)),
				Arguments.of("1.3.30", KotlinVersion(major = 1, minor = 3, patch = 30)),
				Arguments.of("1.3.21", KotlinVersion(major = 1, minor = 3, patch = 21)),
				Arguments.of("1.3.20", KotlinVersion(major = 1, minor = 3, patch = 20)),
				Arguments.of("1.3.11", KotlinVersion(major = 1, minor = 3, patch = 11)),
				Arguments.of("1.3.10", KotlinVersion(major = 1, minor = 3, patch = 10)),
				Arguments.of("1.3.0", KotlinVersion(major = 1, minor = 3, patch = 0)),
				//Arguments.of("1.3.0-rc-198", KotlinVersion(major = 1, minor = 3, patch = 0)),
				//Arguments.of("1.3.0-rc-190", KotlinVersion(major = 1, minor = 3, patch = 0)),
				Arguments.of("1.2.71", KotlinVersion(major = 1, minor = 2, patch = 71)),
				Arguments.of("1.2.70", KotlinVersion(major = 1, minor = 2, patch = 70)),
				Arguments.of("1.2.61", KotlinVersion(major = 1, minor = 2, patch = 61)),
				Arguments.of("1.2.60", KotlinVersion(major = 1, minor = 2, patch = 60)),
				Arguments.of("1.2.51", KotlinVersion(major = 1, minor = 2, patch = 51)),
				Arguments.of("1.2.50", KotlinVersion(major = 1, minor = 2, patch = 50)),
				Arguments.of("1.2.41", KotlinVersion(major = 1, minor = 2, patch = 41)),
				Arguments.of("1.2.40", KotlinVersion(major = 1, minor = 2, patch = 40)),
				Arguments.of("1.2.31", KotlinVersion(major = 1, minor = 2, patch = 31)),
				Arguments.of("1.2.30", KotlinVersion(major = 1, minor = 2, patch = 30)),
				Arguments.of("1.2.21", KotlinVersion(major = 1, minor = 2, patch = 21)),
				Arguments.of("1.2.20", KotlinVersion(major = 1, minor = 2, patch = 20)),
				Arguments.of("1.2.10", KotlinVersion(major = 1, minor = 2, patch = 10)),
				Arguments.of("1.2.0", KotlinVersion(major = 1, minor = 2, patch = 0)),
				Arguments.of("1.1.61", KotlinVersion(major = 1, minor = 1, patch = 61)),
				Arguments.of("1.1.60", KotlinVersion(major = 1, minor = 1, patch = 60)),
				Arguments.of("1.1.51", KotlinVersion(major = 1, minor = 1, patch = 51)),
				Arguments.of("1.1.50", KotlinVersion(major = 1, minor = 1, patch = 50)),
				//Arguments.of("1.1.4-3", KotlinVersion(major = 1, minor = 1, patch = 4)),
				//Arguments.of("1.1.4-2", KotlinVersion(major = 1, minor = 1, patch = 4)),
				Arguments.of("1.1.4", KotlinVersion(major = 1, minor = 1, patch = 4)),
				//Arguments.of("1.1.3-2", KotlinVersion(major = 1, minor = 1, patch = 3)),
				Arguments.of("1.1.3", KotlinVersion(major = 1, minor = 1, patch = 3)),
				//Arguments.of("1.1.2-5", KotlinVersion(major = 1, minor = 1, patch = 2)),
				//Arguments.of("1.1.2-4", KotlinVersion(major = 1, minor = 1, patch = 2)),
				//Arguments.of("1.1.2-3", KotlinVersion(major = 1, minor = 1, patch = 2)),
				//Arguments.of("1.1.2-2", KotlinVersion(major = 1, minor = 1, patch = 2)),
				Arguments.of("1.1.2", KotlinVersion(major = 1, minor = 1, patch = 2)),
				Arguments.of("1.1.1", KotlinVersion(major = 1, minor = 1, patch = 1)),
				Arguments.of("1.1.0", KotlinVersion(major = 1, minor = 1, patch = 0)),
				Arguments.of("1.0.7", KotlinVersion(major = 1, minor = 0, patch = 7)),
				Arguments.of("1.0.6", KotlinVersion(major = 1, minor = 0, patch = 6)),
				//Arguments.of("1.0.5-3", KotlinVersion(major = 1, minor = 0, patch = 5)),
				//Arguments.of("1.0.5-2", KotlinVersion(major = 1, minor = 0, patch = 5)),
				Arguments.of("1.0.5", KotlinVersion(major = 1, minor = 0, patch = 5)),
				Arguments.of("1.0.4", KotlinVersion(major = 1, minor = 0, patch = 4)),
				Arguments.of("1.0.3", KotlinVersion(major = 1, minor = 0, patch = 3)),
				//Arguments.of("1.0.2-1", KotlinVersion(major = 1, minor = 0, patch = 2)),
				Arguments.of("1.0.2", KotlinVersion(major = 1, minor = 0, patch = 2)),
				//Arguments.of("1.0.1-2", KotlinVersion(major = 1, minor = 0, patch = 1)),
				//Arguments.of("1.0.1-1", KotlinVersion(major = 1, minor = 0, patch = 1)),
				Arguments.of("1.0.1", KotlinVersion(major = 1, minor = 0, patch = 1)),
				Arguments.of("1.0.0", KotlinVersion(major = 1, minor = 0, patch = 0)),
				// 0.x and pre-1.0.0 stable are left out, compatibility guarantees don't apply anyway.
			)
	}

	@MethodSource("mavenCentral")
	@ParameterizedTest fun `parse companion extension`(input: String, expected: KotlinVersion) {
		val version = KotlinVersion.parse(input)
		assertEquals(expected, version)
	}

	@ValueSource(
		strings = [
			"", // No components.
			"1", // Too few components.
			"1.2.3.4", // Too many components.
			"v1.2.3", // Invalid character.
			"1.2.3-RC-2", // Invalid format.
			"1.2.3-4", // Unsupported format.
			"1.2.3-Alpha1", // Invalid qualifier.
		]
	)
	@ParameterizedTest fun `parse companion extension bad values`(input: String) {
		assertThrows<IllegalStateException> {
			KotlinVersion.parse(input)
		}
	}
}
