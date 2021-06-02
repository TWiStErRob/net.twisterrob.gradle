package net.twisterrob.gradle.checkstyle.test

/**
 * Usage:
 * ```
 * private lateinit var gradle: GradleRunnerRule
 * private val pmd = CheckstyleTestResources()
 *
 * @Test fun test() {
 *     gradle.file(checkstyle.….config, "config", "checkstyle", "checkstyle.xml")
 * }
 * ```
 */class CheckstyleTestResources {

	val empty = object : EmptyConfiguration {}

	interface EmptyConfiguration {
		val config: String
			get() = read("empty/empty-checkstyle.xml")
	}

	val simple = object : SimpleFailure {}

	interface SimpleFailure {
		val config: String
			get() = read("simple_failure/simple-checkstyle.xml")

		val content: String
			get() = read("simple_failure/empty.java")
	}

	val multi = object : MultiFailure {}

	interface MultiFailure {
		val config: String
			get() = read("multiple_violations/multi-checkstyle.xml")

		val contents: Map<String, String>
			get() =
				listOf(
					"EmptyBlock_3.java",
					"MemberName_2.java",
					"UnusedImports_4.java"
				).associateWith { read("multiple_violations/$it") }
	}

	companion object {
		private fun read(path: String): String =
			fileFromResources(CheckstyleTestResources::class.java, path)
	}
}

private fun fileFromResources(loader: Class<*>, path: String): String {
	val container = "/${loader.`package`.name.replace(".", "/")}"
	val fullPath = "${container}/${path}"
	val resource = loader.getResource(fullPath)
		?: throw IllegalArgumentException("Cannot find ${fullPath}, trying to load ${path} near ${loader}.")
	return resource.readText()
}
