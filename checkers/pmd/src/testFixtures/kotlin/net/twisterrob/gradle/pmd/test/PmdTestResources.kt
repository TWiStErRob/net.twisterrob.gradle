package net.twisterrob.gradle.pmd.test

import net.twisterrob.gradle.test.GradleRunnerRule
import org.gradle.util.GradleVersion

val GradleRunnerRule.pmdRes: PmdTestResources
	get() = PmdTestResources(this)

class PmdTestResources(
	private val gradle: GradleRunnerRule
) {

	val noChecksConfig: String
		get() = read("pmd-empty.xml")

	/**
	 * @see [failingContent]
	 */
	val failingConfig: String
		get() =
			if (gradle.getGradleVersion() < GradleVersion.version("5.0.0")) {
				read("pmd-simple_failure-old.xml")
			} else {
				// Gradle 5+ has PMD 6.x embedded, so they emit a deprecation warning if the old config is used (#81).
				read("pmd-simple_failure.xml")
			}

	/**
	 * @see [failingConfig]
	 */
	val failingContent: String
		get() = read("pmd-simple_failure.java")

	private fun read(path: String): String =
		fileFromResources(PmdTestResources::class.java, path)
}

private fun fileFromResources(loader: Class<*>, path: String): String {
 	val container = "/${loader.`package`.name.replace(".", "/")}"
	val fullPath = "${container}/${path}"
	val resource = loader.getResource(fullPath)
		?: throw IllegalArgumentException("Cannot find ${fullPath}, trying to load ${path} near ${loader}.")
	return resource.readText()
}
