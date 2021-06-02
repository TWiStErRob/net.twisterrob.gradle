package net.twisterrob.gradle.checkstyle.test

import net.twisterrob.gradle.test.GradleRunnerRule

@Suppress("unused") // For consistency with PmdTestResources.
val GradleRunnerRule.checkstyle: CheckstyleTestResources
	get() = CheckstyleTestResources()

class CheckstyleTestResources {

	val empty = object : EmptyConfiguration {}

	interface EmptyConfiguration {
		val config: String
			get() = read("checkstyle-empty.xml")
	}

	val simple = object : SimpleFailure {}

	interface SimpleFailure {
		val config: String
			get() = read("checkstyle-simple_failure.xml")

		val content: String
			get() = read("checkstyle-simple_failure.java")
	}

	val multi = object : MultiFailure {}

	interface MultiFailure {
		val config: String
			get() = read("checkstyle-multiple_violations/checkstyle-template.xml")

		val contents: Map<String, String>
			get() =
				listOf(
					"EmptyBlock_3.java",
					"MemberName_2.java",
					"UnusedImports_4.java"
				).associateWith { read("checkstyle-multiple_violations/$it") }
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
