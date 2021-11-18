package net.twisterrob.gradle.android

import net.twisterrob.gradle.common.AGPVersions

enum class Minification(
	val debugTaskName: String,
	val releaseTaskName: String,
	val gradleProperties: String,
) {

	ProGuard(
		debugTaskName = "minifyDebugWithProguard",
		releaseTaskName = "minifyReleaseWithProguard",
		gradleProperties = """
			android.enableR8=false
		""".trimIndent()
	),
	R8(
		debugTaskName = "minifyDebugWithR8",
		releaseTaskName = "minifyReleaseWithR8",
		gradleProperties = """
			android.enableR8=true
			android.enableR8.fullMode=false
		""".trimIndent()
	),
	R8Full(
		debugTaskName = "minifyDebugWithR8",
		releaseTaskName = "minifyReleaseWithR8",
		gradleProperties = """
			android.enableR8=true
			android.enableR8.fullMode=true
		""".trimIndent()
	),
	;

	companion object {
		/**
		 * @see org.junit.jupiter.params.provider.MethodSource
		 */
		@JvmStatic
		fun agpBasedParams(): List<Minification> =
			when {
				AGPVersions.UNDER_TEST >= AGPVersions.v70x ->
					// ProGuard support was deprecated in AGP 4.2 and removed in AGP 7.0.
					listOf(R8, R8Full)
				else ->
					listOf(ProGuard, R8, R8Full)
			}
	}
}
