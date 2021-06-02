package net.twisterrob.gradle.checkstyle.test

import net.twisterrob.gradle.test.GradleRunnerRule

@Suppress("unused") // For consistency with PmdTestResources.
val GradleRunnerRule.csRes: CheckstyleTestResources
	get() = CheckstyleTestResources()

class CheckstyleTestResources {

	val noChecksConfig: String
		get() = read("checkstyle-empty.xml")

	/**
	 * @see [failingContent]
	 */
	val failingConfig: String
		get() = read("checkstyle-simple_failure.xml")

	/**
	 * @see [failingConfig]
	 */
	val failingContent: String
		get() = read("checkstyle-simple_failure.java")

	/**
	 * @see [multiContents]
	 */
	val multiConfig: String
		get() = read("checkstyle-multiple_violations/checkstyle-template.xml")

	/**
	 * @see [multiConfig]
	 */
	val multiContents: Map<String, String>
		get() =
			listOf(
				"EmptyBlock_3.java",
				"MemberName_2.java",
				"UnusedImports_4.java"
			).associateWith { read("checkstyle-multiple_violations/$it") }

	private fun read(path: String): String =
		fileFromResources(CheckstyleTestResources::class.java, path)
}

private fun fileFromResources(loader: Class<*>, path: String): String {
	val container = "/${loader.`package`.name.replace(".", "/")}"
	val fullPath = "${container}/${path}"
	val resource = loader.getResource(fullPath)
		?: throw IllegalArgumentException("Cannot find ${fullPath}, trying to load ${path} near ${loader}.")
	return resource.readText()
}
