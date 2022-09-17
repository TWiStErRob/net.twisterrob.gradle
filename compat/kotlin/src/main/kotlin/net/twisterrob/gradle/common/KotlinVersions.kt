package net.twisterrob.gradle.common

import org.jetbrains.annotations.TestOnly

/**
 * Version constants for the Kotlin Gradle Plugin.
 */
object KotlinVersions {

	/**
	 * The version KGP on the classpath of the build running this plugin.
	 * Not to be confused with the one used to compile this plugin, which is the latest supported.
	 * In tests, the one on classpath comes from `testImplementation` or `testRuntimeOnly`, use [UNDER_TEST] in that case.
	 *
	 * @see KotlinVersion.CURRENT
	 */
	val CLASSPATH: KotlinVersion
		get() = KotlinVersion.CURRENT

	/**
	 * The test framework is executed with a specific Kotlin Gradle Plugin version.
	 * Used for backwards compatibility testing.
	 */
	val UNDER_TEST: KotlinVersion
		@TestOnly
		get() =
			KotlinVersion.parse(
				System.getProperty("net.twisterrob.test.kotlin.pluginVersion")
					?: error("Property 'net.twisterrob.test.kotlin.pluginVersion' is not set.")
			)
}

private val KOTLIN_VERSION_REGEX: Regex =
	"""^(?<major>\d+)\.(?<minor>\d+)(?:\.(?<patch>\d+))?$""".toRegex()

fun KotlinVersion.Companion.parse(version: String): KotlinVersion {
	val match = KOTLIN_VERSION_REGEX.matchEntire(version)
		?: error("Unrecognised Kotlin Gradle Plugin version: ${version}, only ${KOTLIN_VERSION_REGEX} are supported.")
	val major = match.groups["major"]!!.value.toInt()
	val minor = match.groups["minor"]!!.value.toInt()
	val patch = match.groups["patch"]?.value?.toInt() ?: 0
	return KotlinVersion(major, minor, patch)
}
