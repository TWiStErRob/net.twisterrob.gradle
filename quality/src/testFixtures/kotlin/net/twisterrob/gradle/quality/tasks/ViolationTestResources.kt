package net.twisterrob.gradle.quality.tasks

import java.io.File

@Suppress("detekt.UseDataClass") // https://github.com/detekt/detekt/issues/5339
class ViolationTestResources(
	private val rootProject: File
) {

	val everythingAndroid: EverythingAndroid = EverythingAndroid()

	@Suppress("detekt.StringLiteralDuplication")
	inner class EverythingAndroid : Everything() {

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
		 * @warning do not edit nor commit this file in IDEA.
		 * @see Everything for how to update this.
		 */
		override val violationsXml: String
			get() = read("ViolationTestResources/everything/violations-android.xml")
				.cleanupViolationsXml("junit15407943508211507412")

		/**
		 * @warning do not edit nor commit this file in IDEA.
		 * @see Everything for how to update this.
		 */
		override val violationsHtml: String
			get() = read("ViolationTestResources/everything/violations-android.html")
				.cleanupViolationsHtml("junit15407943508211507412")
	}

	val everythingJvm: EverythingJvm = EverythingJvm()

	@Suppress("detekt.StringLiteralDuplication")
	inner class EverythingJvm : Everything() {

		val checkstyleReport: String
			get() = read("ViolationTestResources/everything/checkstyle-multiple.xml")
				.replace("{{rootProject}}", rootProject.absolutePath)

		val pmdReport: String
			get() = read("ViolationTestResources/everything/pmd-multiple.xml")
				.replace("{{rootProject}}", rootProject.absolutePath)

		/**
		 * @warning do not edit nor commit this file in IDEA.
		 * @see Everything for how to update this.
		 */
		override val violationsXml: String
			get() = read("ViolationTestResources/everything/violations-jvm.xml")
				.cleanupViolationsXml("junit10140171383526166813")

		/**
		 * @warning do not edit nor commit this file in IDEA.
		 * @see Everything for how to update this.
		 */
		override val violationsHtml: String
			get() = read("ViolationTestResources/everything/violations-jvm.html")
				.cleanupViolationsHtml("junit10140171383526166813")
	}

	/**
	 * How to replace `violationsXmlAndroid`/`violationsXmlJvm` and `violationsHtmlAndroid`/`violationsHtmlJvm`?
	 *  1. Run the `runs on multiple * reports` test in [net.twisterrob.gradle.quality.tasks.HtmlReportTaskTest].
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
	abstract inner class Everything {
		abstract val violationsXml: String
		abstract val violationsHtml: String

		@Suppress("detekt.MaxLineLength")
		fun String.cleanupViolationsXml(testFolder: String): String =
			this
				// <violations project="..."
				.replace(
					"""(?<=project=")${testFolder}(?=")""".toRegex(),
					Regex.escapeReplacement(rootProject.name)
				)
				// <location file="..."
				.replace("""(?<=")Z:\\caches\\gradle-test-kit\\${testFolder}((?:\\[^"]+)+)(?=")""".toRegex()) { match ->
					val group1 = match.groupValues[1]
					rootProject.absolutePath + group1.replace("\\", File.separator)
				}
				// <location fileAbsoluteAsUrl="..."
				.replace("""(?<=")file:/Z:/caches/gradle-test-kit/${testFolder}((?:/[^"]+)+)(?=")""".toRegex()) { match ->
					val group1 = match.groupValues[1]
					rootProject.toURI().toString().removeSuffix("/") + group1.replace("\\", File.separator)
				}
				// <violation[details/@rule="IconMissingDensityFolder"]/description
				.replace("""(?<=\\`)Z:\\\\caches\\\\gradle-test-kit\\\\${testFolder}""".toRegex()) {
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

		@Suppress("detekt.MaxLineLength")
		fun String.cleanupViolationsHtml(testFolder: String): String =
			this
				// <title>...
				.replace(
					"""(?<=<title>)${testFolder}""".toRegex(),
					Regex.escapeReplacement(rootProject.name)
				)
				// <h1 id="top">Violations report for ...</h1>
				.replace(
					"""${testFolder}(?=</h1>)""".toRegex(),
					Regex.escapeReplacement(rootProject.name)
				)
				// <script>render.markdown(`...\`...\`...`)</script>
				.replace("""(?<=\\`)Z:\\\\caches\\\\gradle-test-kit\\\\${testFolder}""".toRegex()) {
					// Not replacing the rest of the path with File.separator, because this is just text,
					// but the rootDir is dynamic in the source lint report in test resources.
					rootProject.absolutePath.replace("\\", "\\\\")
				}
				// <a class="file" href="file:/...">src\main\<b>
				.replace("""(?<=")file:/Z:/caches/gradle-test-kit/${testFolder}((?:/[^"]+)+)(?=")""".toRegex()) { match ->
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
