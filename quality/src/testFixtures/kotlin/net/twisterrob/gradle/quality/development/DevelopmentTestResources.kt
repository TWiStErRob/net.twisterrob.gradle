@file:Suppress("unused")

package net.twisterrob.gradle.quality.development

class DevelopmentTestResources {

	val customLint: CustomLint get() = object : CustomLint {}

	interface CustomLint {

		val lintError: LintError get() = object : LintError {}

		interface LintError {
			val xmlReport: String
				get() = read("custom-lint/LintError/lint-results.xml")
		}

		val typographyFractions: TypographyFractions get() = object : TypographyFractions {}

		interface TypographyFractions {
			val violation: String
				get() = read("custom-lint/TypographyFractions/strings.xml")

			val xmlReport: String
				get() = read("custom-lint/TypographyFractions/lint-results.xml")
		}

		val secureRandom: SecureRandom get() = object : SecureRandom {}

		interface SecureRandom {
			val violation: String
				get() = read("custom-lint/SecureRandom/LintFailure.java")

			val xmlReport: String
				get() = read("custom-lint/SecureRandom/lint-results.xml")
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
