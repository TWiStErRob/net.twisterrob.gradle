@file:Suppress("unused")

package net.twisterrob.gradle.quality.development

class DevelopmentTestResources {

	val customLint get() = object : CustomLint {}

	interface CustomLint {

		val lintError get() = object : LintError {}

		interface LintError {
			val xmlReport: String
				get() = read("custom-lint/LintError/lint-results.xml")
		}

		val typographyFractions get() = object : TypographyFractions {}

		interface TypographyFractions {
			val violation: String
				get() = read("custom-lint/TypographyFractions/strings.xml")

			val xmlReport: String
				get() = read("custom-lint/TypographyFractions/lint-results.xml")
		}
	}

	companion object {
		private fun read(path: String): String =
			fileFromResources(DevelopmentTestResources::class.java, path)
	}
}

private fun fileFromResources(loader: Class<*>, path: String): String {
	val container = "/${loader.`package`.name.replace(".", "/")}"
	val fullPath = "${container}/${path}"
	val resource = loader.getResource(fullPath)
		?: throw IllegalArgumentException("Cannot find ${fullPath}, trying to load ${path} near ${loader}.")
	return resource.readText()
}
