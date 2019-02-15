package net.twisterrob.gradle.test

import org.gradle.testkit.runner.GradleRunner
import org.intellij.lang.annotations.Language
import org.junit.rules.TemporaryFolder
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.io.File
import java.net.URI

/**
 * Simplified {@link org.junit.Rule} around {@link GradleRunner} to reduce code repetition.
 */
open class GradleRunnerRule @JvmOverloads constructor(
		clearAfterFailure: Boolean? = null
) : TestRule {

	private val temp = TemporaryFolder()
	private val clearAfterFailure: Boolean = listOfNotNull(
			clearAfterFailure,
			System.getProperty("net.twisterrob.gradle.runner.clearAfterFailure")?.toBoolean(),
			true
	).first()

	private lateinit var buildFile: File
	lateinit var runner: GradleRunner private set

	//region TestRule
	override fun apply(base: Statement, description: Description): Statement {
		return object : Statement() {

			override fun evaluate() {
				var success = false
				try {
					temp.create()
					setUp()
					base.evaluate()
					success = true
				} finally {
					tearDown()
					if (success || clearAfterFailure) {
						temp.delete()
					}
				}
			}
		}
	}
	//endregion

	//region GradleRunner wrapper
	//@Before(automatic with @Rule)
	protected fun setUp() {
		buildFile = temp.newFile("build.gradle")
		runner = GradleRunner
				.create()
				//.forwardOutput() // need to customize forwarding because of test output
				.forwardStdOutput(WriteOnlyWhenLineCompleteWriter(System.out.writer()))
				.forwardStdError(WriteOnlyWhenLineCompleteWriter(System.err.writer()))
				.withProjectDir(temp.root)
				.withPluginClasspath()
		kotlin.assert(this.buildFile == this.getBuildFile()) {
			"Mismatch between internal (${this.buildFile}) and published (${getBuildFile()}) buildFiles."
		}
		fixClassPath(runner)
	}

	protected fun tearDown() {
		// not used yet, but useful for debugging
	}

	//@Test:when
	fun run(@Language("gradle") script: String?, vararg tasks: String): GradleRunner {
		if (script != null) {
			buildFile.appendText(script)
		}
		val args = arrayOf(*tasks, "--stacktrace")
		println("""
			Running `gradle ${args.joinToString(" ")}` on ${buildFile.absolutePath}:
			```gradle
${buildFile.readText().prependIndent("\t\t\t")}
			```
		""".trimIndent())
		return runner.withArguments(*args)
	}

	/**
	 * This is a workaround because runner.withPluginClasspath() doesn't seem to work.
	 */
	private fun fixClassPath(runner: GradleRunner) {
		val buildFile = getBuildFile()
		val classPaths = runner
				.pluginClasspath
				.joinToString(System.lineSeparator()) {
					"classpath files('${it.absolutePath.replace("\\", "\\\\")}')"
				}
		@Language("gradle")
		val buildscript = """
			buildscript {
				dependencies {
${classPaths.prependIndent("\t\t\t\t\t")}
				}
			}
		""".trimIndent() + System.lineSeparator()
		buildFile.appendText(buildscript)
	}
	//endregion

	//region Helper methods

	fun getBuildFile() = File(runner.projectDir, "build.gradle")

	fun settingsFile() = File(runner.projectDir, "settings.gradle")

	//@Test:given/@Before
	fun setGradleVersion(version: String) {
		val distributionUrl = URI.create("https://services.gradle.org/distributions/gradle-${version}-all.zip")
		runner
				.withGradleVersion(version)
				.withGradleDistribution(distributionUrl)
	}

	//@Test:given/@Before
	fun file(contents: String, path: String) {
		file(contents, path.split("""[/\\]""".toRegex()))
	}

	//@Test:given/@Before
	fun file(contents: String, path: Collection<String>) {
		file(contents, *path.toTypedArray())
	}

	fun file(contents: String, vararg path: String) {
		if (path.size == 1 && path[0] == "build.gradle") {
			buildFile.appendText(contents)
			return
		}
		if (1 < path.size) {
			val folders = path.sliceArray(0..path.size - 2)
			if (!File(temp.root, folders.joinToString(File.separator)).exists()) {
				temp.newFolder(*folders)
			}
		}
		temp.newFile(path.joinToString(File.separator)).appendText(contents)
	}

	//@Test:given/@Before
	@JvmOverloads
	fun basedOn(folder: String, relativeTo: Any? = null): GradleRunnerRule {
		return basedOn(templateFile(folder, relativeTo))
	}

	@JvmOverloads
	fun templateFile(path: String, relativeTo: Any? = null): File {
		val container = when (relativeTo) {
			is String -> relativeTo
			null -> ""
			else -> "/${relativeTo.javaClass.`package`.name}"
		}
		val resource = this.javaClass.getResource("${container}/${path}")
				?: throw IllegalArgumentException("Cannot find ${path} relative to {$relativeTo}")
		return File(resource.file)
	}

	//@Test:given/@Before
	fun basedOn(folder: File): GradleRunnerRule {
		println("Deploying ${folder} into ${temp.root}")
		folder.copyRecursively(temp.root, overwrite = false) { file, ex ->
			when {
				file == buildFile && ex is FileAlreadyExistsException -> {
					val originalBuildFile = buildFile.readText()
					val newBuildFile = folder.resolve(buildFile.name).readText()
					buildFile.writeText(originalBuildFile + newBuildFile)
					OnErrorAction.SKIP
				}

				else -> throw ex
			}
		}
		return this
	}
	//endregion
}
