package net.twisterrob.gradle.quality.tasks

import java.io.File

@Suppress("detekt.UseDataClass") // https://github.com/detekt/detekt/issues/5339
class ViolationTestResources(
	private val rootProject: File
) {

	val everything: Everything = Everything()

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
	 *  1. Run the test in `Settings | Build, Execution, Deployment | Build Tools | Gradle`.
	 */
	@Suppress("detekt.StringLiteralDuplication")
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
		@Suppress("detekt.MaxLineLength")
		val violationsXml: String
			get() = read("ViolationTestResources/everything/violations.xml")
				// <violations project="..."
				.replace(
					"""(?<=project=")junit14110403642675258434(?=")""".toRegex(),
					Regex.escapeReplacement(rootProject.name)
				)
				// <location file="..."
				.replace("""(?<=")C:\\Users\\TWiStEr\\AppData\\Local\\Temp\\junit14110403642675258434((?:\\[^"]+)+)(?=")""".toRegex()) { match ->
					val group1 = match.groupValues[1]
					rootProject.absolutePath + group1.replace("\\", File.separator)
				}
				// <location fileAbsoluteAsUrl="..."
				.replace("""(?<=")file:/C:/Users/TWiStEr/AppData/Local/Temp/junit14110403642675258434((?:/[^"]+)+)(?=")""".toRegex()) { match ->
					val group1 = match.groupValues[1]
					rootProject.toURI().toString().removeSuffix("/") + group1.replace("\\", File.separator)
				}
				// <violation[details/@rule="IconMissingDensityFolder"]/description
				.replace("""(?<=\\`)C:\\\\Users\\\\TWiStEr\\\\AppData\\\\Local\\\\Temp\\\\junit14110403642675258434""".toRegex()) {
					// Not replacing the rest of the path with File.separator, because this is just text,
					// but the rootDir is dynamic in the source lint report in test resources.
					rootProject.absolutePath.replace("\\", "\\\\")
				}
				// <location pathRelativeToProject="...\"
				// <location pathRelativeToModule="...\"
				.replace("""(?<=(pathRelativeToProject|pathRelativeToModule)=")(?:[^"]+\\)+(?=")""".toRegex()) { match ->
					match.value.replace("\\", File.separator)
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
		@Suppress("detekt.MaxLineLength")
		val violationsHtml: String
			get() = read("ViolationTestResources/everything/violations.html")
				// <title>...
				.replace(
					"""(?<=<title>)junit14110403642675258434""".toRegex(),
					Regex.escapeReplacement(rootProject.name)
				)
				// <h1 id="top">Violations report for ...</h1>
				.replace(
					"""junit14110403642675258434(?=</h1>)""".toRegex(),
					Regex.escapeReplacement(rootProject.name)
				)
				// <script>render.markdown(`...\`...\`...`)</script>
				.replace("""(?<=\\`)C:\\\\Users\\\\TWiStEr\\\\AppData\\\\Local\\\\Temp\\\\junit14110403642675258434""".toRegex()) {
					// Not replacing the rest of the path with File.separator, because this is just text,
					// but the rootDir is dynamic in the source lint report in test resources.
					rootProject.absolutePath.replace("\\", "\\\\")
				}
				// <a class="file" href="file:/...">src\main\<b>
				.replace("""(?<=")file:/C:/Users/TWiStEr/AppData/Local/Temp/junit14110403642675258434((?:/[^"]+)+)(?=")""".toRegex()) { match ->
					val group1 = match.groupValues[1]
					rootProject.toURI().toString().removeSuffix("/") + group1.replace("\\", File.separator)
				}
				// <a class="file" href="file:/...">...<b>
				.replace("""(a class="file" href="file:/.+">)((?:[^"]+\\)+)(?=<b>)""".toRegex()) { match ->
					val group1 = match.groupValues[1]
					val group2 = match.groupValues[2]
					group1 + group2.replace("\\", File.separator)
				}
				// The XSL transformation will produce system-specific separators
				// (on CI/Unix this is different from the captured Windows line endings).
				// Note: it is convenient that the capture was done on Windows, because it gives an easy target for replacement.
				// If it was captured on Unix, then the replacement would be more complex.
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
