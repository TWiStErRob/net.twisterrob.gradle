package net.twisterrob.gradle.common

object AGPVersions {
	fun olderThan4NotSupported(): Nothing {
		error("AGP $UNDER_TEST is not supported, because it's older than $v4xx")
	}

	val CLASSPATH: AGPVersion
		get() = AGPVersion.parse(ANDROID_GRADLE_PLUGIN_VERSION)

	val UNDER_TEST: AGPVersion
		get() = AGPVersion.parse(System.getProperty("net.twisterrob.test.android.pluginVersion"))

	val v32x: AGPVersion = AGPVersion(3, 2, null, null)
	val v33x: AGPVersion = AGPVersion(3, 3, null, null)
	val v34x: AGPVersion = AGPVersion(3, 4, null, null)
	val v36x: AGPVersion = AGPVersion(3, 6, null, null)
	val v4xx: AGPVersion = AGPVersion(4, null, null, null)
	val v40x: AGPVersion = AGPVersion(4, 0, null, null)
	val v41x: AGPVersion = AGPVersion(4, 1, null, null)
	val v42x: AGPVersion = AGPVersion(4, 2, null, null)
}
