package net.twisterrob.gradle.pmd.test

/**
 * Test resources for PMD static analyser.
 *
 * Usage:
 * ```
 * private lateinit var gradle: GradleRunnerRule
 * private val pmd = PmdTestResources()
 *
 * @Test fun test() {
 *     gradle.file(pmd.….config, "config", "pmd", "pmd.xml")
 * }
 * ```
 */
@Suppress("detekt.UseDataClass") // https://github.com/detekt/detekt/issues/5339
class PmdTestResources {

	val empty: EmptyConfiguration = object : EmptyConfiguration {}
	val simple: SimpleFailures = object : SimpleFailures {}

	interface EmptyConfiguration {
		val config: String
			get() = read("empty/empty-pmd.xml")
	}

	interface SimpleFailures {
		val config: String
			// Gradle 5+ has PMD 6.x embedded, so they emit a deprecation warning if the old config is used (#81).
			get() = read("simple_failures/simple-pmd.xml")

		val content1: String
			get() = read("simple_failures/WithoutPackage.java")

		val message1: Regex
			get() = Regex(
				""".*src.main.java.WithoutPackage\.java:1:\t"""
						+ """NoPackage:\tAll classes, interfaces, enums and annotations must belong to a named package"""
			)

		val content2: String
			get() = read("simple_failures/PrintStack.java")

		val message2: Regex
			get() = Regex(
				""".*src.main.java.pmd.PrintStack\.java:8:\t"""
						+ """AvoidPrintStackTrace:\tAvoid printStackTrace\(\); use a logger call instead\."""
			)
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
