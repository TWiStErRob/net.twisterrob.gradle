package net.twisterrob.gradle.quality.development

class DevelopmentTestResources {

	val customLint = object : CustomLint {}

	interface CustomLint {
		val xml: String
			get() = read("custom-lint/lint-results.xml")
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
