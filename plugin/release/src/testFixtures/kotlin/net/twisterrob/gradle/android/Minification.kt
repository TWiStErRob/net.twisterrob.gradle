package net.twisterrob.gradle.android

import net.twisterrob.gradle.common.AGPVersions
import org.intellij.lang.annotations.Language

enum class Minification(
	val debugTaskName: String,
	val releaseTaskName: String,
	@Language("properties")
	val gradleProperties: String,
) {

	R8(
		debugTaskName = "minifyDebugWithR8",
		releaseTaskName = "minifyReleaseWithR8",
		gradleProperties = """
			${if (AGPVersions.UNDER_TEST >= AGPVersions.v70x) "#" else ""}android.enableR8=true
			android.enableR8.fullMode=false
		""".trimIndent()
	),
	R8Full(
		debugTaskName = "minifyDebugWithR8",
		releaseTaskName = "minifyReleaseWithR8",
		gradleProperties = """
			${if (AGPVersions.UNDER_TEST >= AGPVersions.v70x) "#" else ""}android.enableR8=true
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
					// This also means that `android.enableR8` doesn't exist anymore.
					listOf(R8, R8Full)
				else ->
					AGPVersions.olderThan7NotSupported(AGPVersions.UNDER_TEST)
			}
	}
}
