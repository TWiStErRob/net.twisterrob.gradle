package net.twisterrob.gradle.pmd.test

import net.twisterrob.gradle.test.GradleRunnerRule
import org.gradle.util.GradleVersion

val GradleRunnerRule.pmd: PmdTestResources
	get() = PmdTestResources(this)

class PmdTestResources(
	private val gradle: GradleRunnerRule
) {

	val empty = object : EmptyConfiguration {}

	interface EmptyConfiguration {
		val config: String
			get() = read("empty/empty-pmd.xml")
	}

	val simple = SimpleFailure()

	inner class SimpleFailure {
		val config: String
			get() =
				if (gradle.getGradleVersion() < GradleVersion.version("5.0.0")) {
					read("simple_failure/simple_old-pmd.xml")
				} else {
					// Gradle 5+ has PMD 6.x embedded, so they emit a deprecation warning if the old config is used (#81).
					read("simple_failure/simple-pmd.xml")
				}

		val content: String
			get() = read("simple_failure/WithoutPackage.java")
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
