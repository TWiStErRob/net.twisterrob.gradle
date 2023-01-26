rootProject {
	val gradleTasks = gradle.startParameter.taskNames
	val gradleBuildFile = this.buildFile
	val gradleVersion = gradle.gradleVersion
	val gradleTestWorkerId = System.getProperty("org.gradle.test.worker")
	val gradleHome = gradle.gradleUserHomeDir
	val gradleDir = gradle.gradleHomeDir
	val javaVendor = System.getProperty("java.vendor")
	val javaVersion = System.getProperty("java.version")
	val javaVersionDate = System.getProperty("java.version.date")
	val javaRuntimeName = System.getProperty("java.runtime.name")
	val javaRuntimeVersion = System.getProperty("java.runtime.version")
	val javaHome = System.getProperty("java.home")
	val javaHomeEnv = System.getenv("JAVA_HOME")
	@Suppress("NullableToStringCall") // Debug info, null is OK.
	val java = "${javaVendor} ${javaRuntimeName} ${javaVersion} (${javaRuntimeVersion} ${javaVersionDate})"

	println( // Using println rather than logger.lifecycle() to make sure it outputs even with --quiet.
		"""
		Gradle ${gradleVersion} worker #${gradleTestWorkerId} at ${gradleHome.absolutePath} with ${gradleDir?.absolutePath}.
		Gradle Java: ${java} from ${javaHome}${if (javaHome != javaHomeEnv) " (JAVA_HOME = ${javaHomeEnv})" else ""}.
		Running `gradle ${gradleTasks.joinToString(" ")}` on ${gradleBuildFile.absolutePath}.
	""".trimIndent()
	)
}
