package net.twisterrob.gradle.pmd.test

import org.gradle.util.GradleVersion

/**
 * Usage:
 * ```
 * private lateinit var gradle: GradleRunnerRule
 * private val pmd = PmdTestResources { gradle.getGradleVersion() }
 *
 * @Test fun test() {
 *     gradle.file(pmd.….config, "config", "pmd", "pmd.xml")
 * }
 * ```
 */
class PmdTestResources(
	private val gradleVersion: () -> GradleVersion
) {

	val empty: EmptyConfiguration get() = object : EmptyConfiguration {}

	interface EmptyConfiguration {
		val config: String
			get() = read("empty/empty-pmd.xml")
	}

	val simple: SimpleFailure get() = SimpleFailure()

	inner class SimpleFailure {
		val config: String
			get() =
				if (gradleVersion() < GradleVersion.version("5.0.0")) {
					read("simple_failure/simple_old-pmd.xml")
				} else {
					// Gradle 5+ has PMD 6.x embedded, so they emit a deprecation warning if the old config is used (#81).
					read("simple_failure/simple-pmd.xml")
				}

		val content: String
			get() = read("simple_failure/WithoutPackage.java")

		val message: Regex
			get() =
				when {
					gradleVersion() < GradleVersion.version("5.6.0") -> {
						Regex(""".*src.main.java.Pmd\.java:1:\tAll classes and interfaces must belong to a named package""")
					}
					gradleVersion() < GradleVersion.version("7.0.0") -> {
						Regex(""".*src.main.java.Pmd\.java:1:\tAll classes, interfaces, enums and annotations must belong to a named package""")
					}
					else -> {
						Regex(""".*src.main.java.Pmd\.java:1:\tNoPackage:\tAll classes, interfaces, enums and annotations must belong to a named package""")
					}
				}
	}

	companion object {
		private fun read(path: String): String =
			fileFromResources(PmdTestResources::class.java, path)
	}
}

private fun fileFromResources(loader: Class<*>, path: String): String {
	val container = "/${loader.`package`.name.replace(".", "/")}"
	val fullPath = "${container}/${path}"
	val resource = loader.getResource(fullPath)
		?: throw IllegalArgumentException("Cannot find ${fullPath}, trying to load ${path} near ${loader}.")
	return resource.readText()
}
