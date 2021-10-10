package net.twisterrob.gradle.android

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
}
