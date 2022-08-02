package net.twisterrob.gradle.quality.tasks

import java.io.File

class ViolationTestResources(
	private val rootProject: File
) {

	val everything: Everything get() = Everything()

	inner class Everything {
		val checkstyleReport: String
			get() = read("ViolationTestResources/everything/checkstyle-multiple.xml")
				.replace("{{rootProject}}", rootProject.absolutePath)

		val pmdReport: String
			get() = read("ViolationTestResources/everything/pmd-multiple.xml")
				.replace("{{rootProject}}", rootProject.absolutePath)

		val lintReport: String
			get() = read("ViolationTestResources/everything/lint-multiple.xml")
				.replace("{{rootProject}}", rootProject.absolutePath)

		/**
		 * @warning DO NOT EDIT (https://youtrack.jetbrains.com/issue/IDEA-171699)
		 * NOR COMMIT THIS FILE IN IDEA (https://youtrack.jetbrains.com/issue/IDEA-79736)
		 */
		@Suppress("MaxLineLength")
		val violationsXml: String
			get() = read("ViolationTestResources/everything/violations.xml")
				// <violations project="..."
				.replace(
					"""(?<=project=")junit12252005098066695430(?=")""".toRegex(),
					Regex.escapeReplacement(rootProject.name)
				)
				// <location file="..."
				.replace(
					"""(?<=")C:\\Users\\TWiStEr\\AppData\\Local\\Temp\\junit12252005098066695430((?:\\.+)+)(?=")""".toRegex(),
					Regex.escapeReplacement(rootProject.absolutePath) + "$1"
				)
				// <location fileAbsoluteAsUrl="..."
				.replace(
					"""(?<=")file:/C:/Users/TWiStEr/AppData/Local/Temp/junit12252005098066695430((?:/.+)+)(?=")""".toRegex(),
					Regex.escapeReplacement(rootProject.toURI().toString().removeSuffix("/")) + "$1"
				)
				// <violation[details/@rule="IconMissingDensityFolder"]/description
				.replace(
					"""(?<=\\`)C:\\\\Users\\\\TWiStEr\\\\AppData\\\\Local\\\\Temp\\\\junit12252005098066695430((?:\\\\.+)+)(?=\\`)""".toRegex(),
					Regex.escapeReplacement(rootProject.absolutePath.replace("\\", "\\\\")) + "$1"
				)
				// The XSL transformation will produce system-specific separators
				// (on CI/Unix this is different than the captured Windows line endings).
				.replace("\r\n", System.lineSeparator())

		/**
		 * @warning DO NOT EDIT (https://youtrack.jetbrains.com/issue/IDEA-171699)
		 * NOR COMMIT THIS FILE IN IDEA (https://youtrack.jetbrains.com/issue/IDEA-79736)
		 */
		@Suppress("MaxLineLength")
		val violationsHtml: String
			get() = read("ViolationTestResources/everything/violations.html")
				// <title>...
				.replace(
					"""(?<=<title>)junit12252005098066695430""".toRegex(),
					Regex.escapeReplacement(rootProject.name)
				)
				// <h1 id="top">Violations report for ...</h1>
				.replace(
					"""junit12252005098066695430(?=</h1>)""".toRegex(),
					Regex.escapeReplacement(rootProject.name)
				)
				// <script>render.markdown(`...\`...\`...`)</script>
				.replace(
					"""(?<=\\`)C:\\\\Users\\\\TWiStEr\\\\AppData\\\\Local\\\\Temp\\\\junit12252005098066695430((?:\\\\.+)+)(?=\\`)""".toRegex(),
					Regex.escapeReplacement(rootProject.absolutePath.replace("\\", "\\\\")) + "$1"
				)
				// <a class="file" href="file:/..."
				.replace(
					"""(?<=")file:/C:/Users/TWiStEr/AppData/Local/Temp/junit12252005098066695430((?:/.+)+)(?=")""".toRegex(),
					Regex.escapeReplacement(rootProject.toURI().toString().removeSuffix("/")) + "$1"
				)
				// The XSL transformation will produce system-specific separators
				// (on CI/Unix this is different than the captured Windows line endings).
				.replace("\r\n", System.lineSeparator())
	}

	companion object {
		private fun read(path: String): String =
			fileFromResources(ViolationTestResources::class.java, path)
	}
}

private fun fileFromResources(loader: Class<*>, path: String): String {
	val container = "/${loader.`package`.name.replace(".", "/")}"
	val fullPath = "${container}/${path}"
	val resource = loader.getResource(fullPath)
		?: throw IllegalArgumentException("Cannot find ${fullPath}, trying to load ${path} near ${loader}.")
	return resource.readText()
}
