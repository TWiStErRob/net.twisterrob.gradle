package net.twisterrob.gradle.android

object SdkVersions {

	val testCompileSdkVersion: Int =
		System.getProperty("net.twisterrob.test.android.compileSdkVersion")
			.removePrefix("android-")
			.toInt()

	/**
	 * Calculates versionName from SDK level.
	 */
	@Suppress("CyclomaticComplexMethod")
	fun Int.asName(): String =
		@Suppress("MagicNumber") // SDK levels are magic numbers, see https://apilevels.com
		when (this) {
			35 -> "15"
			34 -> "14"
			33 -> "13"
			32 -> "12"
			31 -> "12"
			30 -> "11"
			29 -> "10"
			28 -> "9"
			27 -> "8.1"
			26 -> "8.0"
			25 -> "7.1"
			24 -> "7.0"
			23 -> "6.0"
			22 -> "5.1"
			21 -> "5.0"
			else -> error("Unsupported level: ${this}")
		}
}
