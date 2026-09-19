package net.twisterrob.gradle.android

import org.intellij.lang.annotations.Language

enum class Minification(
	val debugTaskName: String,
	val releaseTaskName: String,
	@param:Language("properties")
	val gradleProperties: String,
) {

	// ProGuard support was deprecated in AGP 4.2 and removed in AGP 7.0.
	// This also means that `android.enableR8` doesn't exist anymore.
	//ProGuard(
	//	debugTaskName = "minifyDebugWithProguard",
	//	releaseTaskName = "minifyReleaseWithProguard",
	//	gradleProperties = """
	//		android.enableR8=false
	//	""".trimIndent()
	//),

	R8(
		debugTaskName = "minifyDebugWithR8",
		releaseTaskName = "minifyReleaseWithR8",
		gradleProperties = """
			android.enableR8.fullMode=false
		""".trimIndent()
	),

	R8Full(
		debugTaskName = "minifyDebugWithR8",
		releaseTaskName = "minifyReleaseWithR8",
		gradleProperties = """
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
			Minification.values().toList()
	}
}
