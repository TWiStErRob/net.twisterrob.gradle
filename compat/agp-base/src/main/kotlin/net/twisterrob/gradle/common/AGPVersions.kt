package net.twisterrob.gradle.common

import org.jetbrains.annotations.TestOnly

/**
 * Version constants for the Android Gradle Plugin.
 */
@Suppress("MagicNumber") // Meant for hardcoding version numbers.
object AGPVersions {

	/**
	 * The version AGP on the classpath of the build running this plugin.
	 * Not to be confused with the one used to compile this plugin, which is the latest supported.
	 * In tests, the one on classpath comes from `testImplementation` or `testRuntimeOnly`, use [UNDER_TEST] in that case.
	 *
	 * @see com.android.Version
	 * @throws IllegalStateException if there's no AGP on the classpath.
	 * @throws IllegalStateException when the version number is not recognized.
	 */
	val CLASSPATH: AGPVersion
		@Throws(IllegalStateException::class)
		get() =
			AGPVersion.parse(ANDROID_GRADLE_PLUGIN_VERSION)

	/**
	 * The test framework is executed with a specific AGP version.
	 * Used for backwards compatibility testing.
	 */
	val UNDER_TEST: AGPVersion
		@TestOnly
		get() =
			AGPVersion.parse(
				System.getProperty("net.twisterrob.test.android.pluginVersion")
					?: error("Property 'net.twisterrob.test.android.pluginVersion' is not set.")
			)

	val v7xx: AGPVersion = AGPVersion(major = 7, minor = null, type = null, patch = null)
	val v70x: AGPVersion = AGPVersion(major = 7, minor = 0, type = null, patch = null)
	val v71x: AGPVersion = AGPVersion(major = 7, minor = 1, type = null, patch = null)
	val v72x: AGPVersion = AGPVersion(major = 7, minor = 2, type = null, patch = null)
	val v73x: AGPVersion = AGPVersion(major = 7, minor = 3, type = null, patch = null)
	val v74x: AGPVersion = AGPVersion(major = 7, minor = 4, type = null, patch = null)
	val v80x: AGPVersion = AGPVersion(major = 8, minor = 0, type = null, patch = null)
	val v81x: AGPVersion = AGPVersion(major = 8, minor = 1, type = null, patch = null)

	/**
	 * Is there an Android Gradle Plugin on the classpath?
	 *
	 * If so, it must have a version, and [CLASSPATH] is usable.
	 *
	 * @see CLASSPATH
	 */
	val isAvailable: Boolean
		get() =
			try {
				ANDROID_GRADLE_PLUGIN_VERSION
				true
			} catch (ignore: IllegalStateException) {
				false
			}

	@Throws(IllegalStateException::class)
	fun olderThan7NotSupported(version: AGPVersion): Nothing {
		error("AGP ${version} is not supported, because it's older than ${v7xx}")
	}
}

/**
 * Determines the version of Android Gradle Plugin on the classpath.
 * AGP Version keeps moving around between versions.
 *
 * @throws IllegalStateException if there's no AGP on the classpath.
 */
private val ANDROID_GRADLE_PLUGIN_VERSION: String
	@Throws(IllegalStateException::class)
	get() {
		val versionClass: Class<*> =
			// Cannot use compatibility AGPVersions.CLASSPATH to create a `when` in this one, as this is defining it.
			kotlin
				.runCatching {
					// Introduced in AGP 3.6.x and live in 7.3.1; 7.4.0 changed bytecode version to 55 (Java 11).
					Class.forName("com.android.Version")
				}
				.recoverCatching { ex ->
					if (ex !is ClassNotFoundException) throw ex
					// Deprecated in AGP 3.6.x and removed in AGP 4.x.
					Class.forName("com.android.builder.model.Version")
				}
				.recoverCatching { ex ->
					throw IllegalStateException("Cannot find AGP Version class on the classpath", ex)
				}
				.getOrThrow()
		return versionClass.getDeclaredField("ANDROID_GRADLE_PLUGIN_VERSION").get(null) as String
	}
