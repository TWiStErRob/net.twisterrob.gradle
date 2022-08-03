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
				.replace("""(?<=")C:\\Users\\TWiStEr\\AppData\\Local\\Temp\\junit12252005098066695430((?:\\.+)+)(?=")""".toRegex()) {
					val group1 = it.groups[1]!!.value
					rootProject.absolutePath + group1.replace("\\", File.separator)
				}
				// <location fileAbsoluteAsUrl="..."
				.replace("""(?<=")file:/C:/Users/TWiStEr/AppData/Local/Temp/junit12252005098066695430((?:/.+)+)(?=")""".toRegex()) {
					val group1 = it.groups[1]!!.value
					rootProject.toURI().toString().removeSuffix("/") + group1.replace("\\", File.separator)
				}
				// <violation[details/@rule="IconMissingDensityFolder"]/description
				.replace("""(?<=\\`)C:\\\\Users\\\\TWiStEr\\\\AppData\\\\Local\\\\Temp\\\\junit12252005098066695430((?:\\\\.+)+)(?=\\`)""".toRegex()) {
					val group1 = it.groups[1]!!.value
					rootProject.absolutePath.replace("\\", "\\\\") + group1.replace("\\", File.separator)
				}
				// <location pathRelativeToProject="...\"
				// <location pathRelativeToModule="...\"
				.replace("""(?<=(pathRelativeToProject|pathRelativeToModule)=")(?:.+\\)+(?=")""".toRegex()) {
					it.value.replace("\\", File.separator)
				}
				// The XSL transformation will produce system-specific separators
				// (on CI/Unix this is different from the captured Windows line endings).
				.replace("\r\n", System.lineSeparator())
				.replace("&#xa;", "\r")

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
				.replace("""(?<=\\`)C:\\\\Users\\\\TWiStEr\\\\AppData\\\\Local\\\\Temp\\\\junit12252005098066695430((?:\\\\.+)+)(?=\\`)""".toRegex()) {
					val group1 = it.groups[1]!!.value
					rootProject.absolutePath.replace("\\", "\\\\") +
							group1.replace("\\\\", File.separator.replace("\\", "\\\\"))
				}
				// <a class="file" href="file:/...">src\main\<b>
				.replace("""(?<=")file:/C:/Users/TWiStEr/AppData/Local/Temp/junit12252005098066695430((?:/.+)+)(?=")""".toRegex()) {
					val group1 = it.groups[1]!!.value
					rootProject.toURI().toString().removeSuffix("/") + group1.replace("\\", File.separator)
				}
				// <a class="file" href="file:/...">...<b>
				.replace("""(a class="file" href="file:/.+">)((?:.+\\)+)(?=<b>)""".toRegex()) {
					val group1 = it.groups[1]!!.value
					val group2 = it.groups[2]!!.value
					group1 + group2.replace("\\", File.separator)
				}
				// The XSL transformation will produce system-specific separators
				// (on CI/Unix this is different from the captured Windows line endings).
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
