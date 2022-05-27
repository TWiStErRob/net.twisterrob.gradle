package net.twisterrob.gradle.detekt.test

/**
 * Usage:
 * ```
 * private lateinit var gradle: GradleRunnerRule
 * private val pmd = DetektTestResources()
 *
 * @Test fun test() {
 *     gradle.file(detekt.….config, "config", "detekt", "detekt.yml")
 * }
 * ```
 */
class DetektTestResources {

	val empty: EmptyConfiguration = object : EmptyConfiguration {}

	interface EmptyConfiguration {
		val config: String
			get() = read("empty/empty-detekt.yml")
	}

	val simple: SimpleFailure = object : SimpleFailure {}

	interface SimpleFailure {
		val config: String
			get() = read("simple_failure/simple-detekt.yml")

		val content: String
			get() = read("simple_failure/empty.kt")

		val message: Regex
			get() = Regex(""".*src.main.java.Detekt\.kt:1: .*? \[Header]""")
	}

	companion object {
		private fun read(path: String): String =
			fileFromResources(DetektTestResources::class.java, path)
	}
}

private fun fileFromResources(loader: Class<*>, path: String): String {
	val container = "/${loader.`package`.name.replace(".", "/")}"
	val fullPath = "${container}/${path}"
	val resource = loader.getResource(fullPath)
		?: throw IllegalArgumentException("Cannot find ${fullPath}, trying to load ${path} near ${loader}.")
	return resource.readText()
}
