package net.twisterrob.gradle.quality.tasks

import java.io.File
import java.lang.management.ManagementFactory

class ViolationTestResources(
	private val rootProject: File
) {

	val everything: Everything get() = Everything()

	/**
	 * How to replace [violationsXml] and [violationsHtml]?
	 *  1. Run the [net.twisterrob.gradle.quality.tasks.HtmlReportTaskTest.runs on multiple reports] test..
	 *  1. Check the output in `:quality/build/reports/tests/test/outputs`.
	 *  1. Overwrite the file in `src/test/resources`.
	 *  1. Commit the change externally via git command line.
	 *    * See [Keep different line separators in the file](https://youtrack.jetbrains.com/issue/IDEA-171699)
	 *    * See [Committing a mixed-EOL will convert all the EOLs](https://youtrack.jetbrains.com/issue/IDEA-79736)
	 *  1. Replace the project name (junit...) in this file with the new name.
	 *  1. Run the test again to validate.
	 *  1. Amend the commit to include changes in this file.
	 */
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
		 * @see Everything for how to update this.
		 */
		@Suppress("MaxLineLength")
		val violationsXml: String
			get() = read("ViolationTestResources/everything/violations.xml")
				// <violations project="..."
				.replace(
					"""(?<=project=")junit10604310516655983690(?=")""".toRegex(),
					Regex.escapeReplacement(rootProject.name)
				)
				// <location file="..."
				.replace("""(?<=")C:\\Users\\TWiStEr\\AppData\\Local\\Temp\\junit10604310516655983690((?:\\[^"]+)+)(?=")""".toRegex()) {
					val group1 = it.groups[1]!!.value
					rootProject.absolutePath + group1.replace("\\", File.separator)
				}
				// <location fileAbsoluteAsUrl="..."
				.replace("""(?<=")file:/C:/Users/TWiStEr/AppData/Local/Temp/junit10604310516655983690((?:/[^"]+)+)(?=")""".toRegex()) {
					val group1 = it.groups[1]!!.value
					rootProject.toURI().toString().removeSuffix("/") + group1.replace("\\", File.separator)
				}
				// <violation[details/@rule="IconMissingDensityFolder"]/description
				.replace("""(?<=\\`)C:\\\\Users\\\\TWiStEr\\\\AppData\\\\Local\\\\Temp\\\\junit10604310516655983690""".toRegex()) {
					// Not replacing the rest of the path with File.separator, because this is just text,
					// but the rootDir is dynamic in the source lint report in test resources.
					rootProject.absolutePath.replace("\\", "\\\\")
				}
				// <location pathRelativeToProject="...\"
				// <location pathRelativeToModule="...\"
				.replace("""(?<=(pathRelativeToProject|pathRelativeToModule)=")(?:[^"]+\\)+(?=")""".toRegex()) {
					it.value.replace("\\", File.separator)
				}
				// The XSL transformation will produce system-specific separators
				// (on CI/Unix this is different from the captured Windows line endings).
				.replace("\r\n", System.lineSeparator())
				// Prepare the expected XML to match behavior of executing environment.
				.run {
					if (System.lineSeparator() == "\n") {
						// On the Unix CI the XML transformation outputs &#xa; instead of new line character.
						this.replace("&#xa;", "\n")
					} else {
						// On local Windows this seems to work fine as is.
						this
					}
				}

		/**
		 * @warning DO NOT EDIT (https://youtrack.jetbrains.com/issue/IDEA-171699)
		 * NOR COMMIT THIS FILE IN IDEA (https://youtrack.jetbrains.com/issue/IDEA-79736)
		 * @see Everything for how to update this.
		 */
		@Suppress("MaxLineLength")
		val violationsHtml: String
			get() = read("ViolationTestResources/everything/violations.html")
				// <title>...
				.replace(
					"""(?<=<title>)junit10604310516655983690""".toRegex(),
					Regex.escapeReplacement(rootProject.name)
				)
				// <h1 id="top">Violations report for ...</h1>
				.replace(
					"""junit10604310516655983690(?=</h1>)""".toRegex(),
					Regex.escapeReplacement(rootProject.name)
				)
				// <script>render.markdown(`...\`...\`...`)</script>
				.replace("""(?<=\\`)C:\\\\Users\\\\TWiStEr\\\\AppData\\\\Local\\\\Temp\\\\junit10604310516655983690""".toRegex()) {
					// Not replacing the rest of the path with File.separator, because this is just text,
					// but the rootDir is dynamic in the source lint report in test resources.
					rootProject.absolutePath.replace("\\", "\\\\")
				}
				// <a class="file" href="file:/...">src\main\<b>
				.replace("""(?<=")file:/C:/Users/TWiStEr/AppData/Local/Temp/junit10604310516655983690((?:/[^"]+)+)(?=")""".toRegex()) {
					val group1 = it.groups[1]!!.value
					rootProject.toURI().toString().removeSuffix("/") + group1.replace("\\", File.separator)
				}
				// <a class="file" href="file:/...">...<b>
				.replace("""(a class="file" href="file:/.+">)((?:[^"]+\\)+)(?=<b>)""".toRegex()) {
					val group1 = it.groups[1]!!.value
					val group2 = it.groups[2]!!.value
					group1 + group2.replace("\\", File.separator)
				}
				// The XSL transformation on Java 8 doesn't acknowledge the indent="true" attribute,
				// so we need to clean up the whitespace to match it.
				.run {
					if (ManagementFactory.getRuntimeMXBean().specVersion == "1.8") {
						this
							.replace("""(?m)^ +(?=\t*)""".toRegex(), "")
							.replace("""(?<=\))\r\n\t\t\t\r\n(?=</li>)""".toRegex(), "\r\n\t\t\t")
							.replace(
								"""(?<=<div class="violation" xml:space="preserve">)\r\n\t\t\t(?=<span class="title")""".toRegex(),
								"\r\n\t\t\t\r\n"
							)
					} else {
						this
					}
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
