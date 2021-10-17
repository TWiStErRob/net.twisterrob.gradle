package net.twisterrob.gradle.common

import org.jetbrains.annotations.TestOnly

/**
 * Version constants for the Android Gradle Plugin.
 */
object AGPVersions {

	fun olderThan4NotSupported(): Nothing {
		error("AGP $UNDER_TEST is not supported, because it's older than $v4xx")
	}

	/**
	 *
	 */
	val CLASSPATH: AGPVersion
		get() = AGPVersion.parse(ANDROID_GRADLE_PLUGIN_VERSION)

	/**
	 * The test framework is executed with a specific AGP version.
	 */
	val UNDER_TEST: AGPVersion
		@TestOnly
		get() = AGPVersion.parse(System.getProperty("net.twisterrob.test.android.pluginVersion"))

	val v32x: AGPVersion = AGPVersion(3, 2, null, null)
	val v33x: AGPVersion = AGPVersion(3, 3, null, null)
	val v34x: AGPVersion = AGPVersion(3, 4, null, null)
	val v36x: AGPVersion = AGPVersion(3, 6, null, null)
	val v4xx: AGPVersion = AGPVersion(4, null, null, null)
	val v40x: AGPVersion = AGPVersion(4, 0, null, null)
	val v41x: AGPVersion = AGPVersion(4, 1, null, null)
	val v42x: AGPVersion = AGPVersion(4, 2, null, null)

	/**
	 * Cannot use compatibility AGPVersions in this one, as this is defining [CLASSPATH].
	 */
	private val ANDROID_GRADLE_PLUGIN_VERSION: String
		get() {
			val versionClass: Class<*> =
				try {
					// Introduced in AGP 3.6.x.
					Class.forName("com.android.Version")
				} catch (ex: Throwable) {
					// Deprecated in AGP 3.6.x and removed in AGP 4.x.
					Class.forName("com.android.builder.model.Version")
				}
			return versionClass.getDeclaredField("ANDROID_GRADLE_PLUGIN_VERSION").get(null) as String
		}
}
