package net.twisterrob.gradle.common

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedInvocationConstants.INDEX_PLACEHOLDER
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junitpioneer.jupiter.ClearSystemProperty
import org.junitpioneer.jupiter.SetSystemProperty
import kotlin.reflect.full.declaredMembers

class AGPVersionsTest {

	@Test fun `olderThan81NotSupported returns the right message`() {
		@Suppress("detekt.NamedArguments")
		val version = AGPVersion(1, 2, AGPVersion.ReleaseType.Stable, 4)

		val ex = assertThrows<IllegalStateException> {
			AGPVersions.olderThan81NotSupported(version)
		}

		assertEquals("AGP 1.2.Stable.4 is not supported, because it's older than 8.1.*.*", ex.message)
	}

	@Test fun `CLASSPATH version is what the project is compiled with`() {
		// This is not using AGPVersion() because Renovate needs to update this one. See "Update AGP version test.".
		val expected = AGPVersion.parse("8.11.2")

		val actual = AGPVersions.CLASSPATH

		assertEquals(expected, actual)
	}

	@SetSystemProperty(key = "net.twisterrob.test.android.pluginVersion", value = "1.2.3")
	@Test fun `UNDER_TEST reads system property`() {
		@Suppress("detekt.NamedArguments")
		val expected = AGPVersion(1, 2, AGPVersion.ReleaseType.Stable, 3)

		val actual = AGPVersions.UNDER_TEST

		assertEquals(expected, actual)
	}

	@SetSystemProperty(key = "net.twisterrob.test.android.pluginVersion", value = "x.y.z")
	@Test fun `UNDER_TEST fails when system property invalid`() {
		val ex = assertThrows<IllegalStateException> {
			AGPVersions.UNDER_TEST
		}

		assertThat(ex.message, containsString("x.y.z"))
	}

	@ClearSystemProperty(key = "net.twisterrob.test.android.pluginVersion")
	@Test fun `UNDER_TEST fails when system property missing`() {
		val ex = assertThrows<IllegalStateException> {
			AGPVersions.UNDER_TEST
		}

		assertThat(ex.message, containsString("net.twisterrob.test.android.pluginVersion"))
	}

	@Suppress("detekt.UnreachableCode") // REPORT Looks like `val name`'s ?: somehow produces a false positive.
	@CsvSource(
		"8, 1",
		"8, 3",
		"8, 4",
	)
	@ParameterizedTest(name = "[$INDEX_PLACEHOLDER] v{0}.{1}.x")
	fun `vXXX constants have the right version`(major: Int, minor: Int?) {
		val name = "v${major}${minor ?: 'x'}x"
		val member = AGPVersions::class.declaredMembers.singleOrNull { it.name == name }
			?: fail("Cannot find $name field")

		val actual = member.call(AGPVersions)

		@Suppress("detekt.NamedArguments")
		val expected = AGPVersion(major, minor, null, null)
		assertEquals(expected, actual)
	}
}
