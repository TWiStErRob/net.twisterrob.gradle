package net.twisterrob.gradle.test

import org.gradle.api.internal.tasks.testing.worker.TestWorker
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.internal.DefaultGradleRunner
import org.gradle.util.GradleVersion
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
open class GradleRunnerRule : TestRule {

	private val temp = TemporaryFolder()

	/**
	 * Defines if a tests Gradle folder should be removed after a failed execution.
	 * Precedence:
	 *  * this property (`null` &rarr; next)
	 *  * `net.twisterrob.gradle.runner.clearAfterFailure` system property (not set &rarr; next)
	 *  * automatically clear (when none of the above are specified)
	 *
	 * @param value `null` = automatic, `true` clean, `false` keep
	 * @see GradleRunner.getProjectDir `runner.projectDir`
	 */
	@Suppress("KDocUnresolvedReference")
	var clearAfterFailure: Boolean? = null
	private val needClearAfterFailure: Boolean
		get() = listOfNotNull(
			clearAfterFailure,
			System.getProperty("net.twisterrob.gradle.runner.clearAfterFailure")?.toBoolean(),
			true
		).first()

	/**
	 * Defines if a tests Gradle folder should be removed after a failed execution.
	 * Precedence:
	 *  * this property (`null` &rarr; next)
	 *  * `net.twisterrob.gradle.runner.clearAfterFailure` system property (not set &rarr; next)
	 *  * automatically clear (when none of the above are specified)
	 *
	 * @param value `null` = automatic, `true` clean, `false` keep
	 * @see GradleRunner.getProjectDir `runner.projectDir`
	 */
	@Suppress("KDocUnresolvedReference")
	var clearAfterSuccess: Boolean? = null
	private val needClearAfterSuccess: Boolean
		get() = listOfNotNull(
			clearAfterSuccess,
			System.getProperty("net.twisterrob.gradle.runner.clearAfterSuccess")?.toBoolean(),
			true
		).first()

	lateinit var runner: GradleRunner private set

	@Suppress("MemberVisibilityCanBePrivate") // API
	lateinit var buildFile: File
		private set

	val settingsFile: File
		get() = File(runner.projectDir, "settings.gradle")

	val propertiesFile: File
		get() = File(runner.projectDir, "gradle.properties")

	//region TestRule
	override fun apply(base: Statement, description: Description): Statement {
		return object : Statement() {

			override fun evaluate() {
				var success = false
				try {
					before()
					base.evaluate()
					success = true
				} finally {
					after(success)
				}
			}
		}
	}

	internal fun before() {
		temp.create()
		setUp()
	}

	internal fun after(success: Boolean) {
		tearDown()
		if ((success && needClearAfterSuccess) || (!success && needClearAfterFailure)) {
			temp.delete()
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
		check(buildFile == File(runner.projectDir, "build.gradle")) {
			"${buildFile} is not within ${runner.projectDir}."
		}
		fixClassPath(runner)
		System.getProperty("net.twisterrob.gradle.runner.gradleVersion", null)?.let {
			gradleVersion = GradleVersion.version(it)
		}
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
		val gradleTestWorkerId: String? by systemProperty(TestWorker.WORKER_ID_SYS_PROPERTY)
		val testKitDir = runner.let { it as? DefaultGradleRunner }?.testKitDirProvider?.dir
		println(
			"""
			Gradle worker #${gradleTestWorkerId} at ${testKitDir?.absolutePath}.
			Running `gradle ${args.joinToString(" ")}` on ${buildFile.absolutePath}:
			```gradle
${buildFile.readText().prependIndent("\t\t\t")}
			```
			Execution output:
			""".trimIndent()
		)
		return runner.withArguments(*args)
	}

	/**
	 * This is a workaround because runner.withPluginClasspath() doesn't seem to work.
	 */
	private fun fixClassPath(runner: GradleRunner) {
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

	//@Test:given/@Before
	@Deprecated(
		message = "Use gradleVersion property instead.",
		replaceWith = ReplaceWith("gradleVersion = GradleVersion.version(version)")
	)
	fun setGradleVersion(version: String) {
		gradleVersion = GradleVersion.version(version)
	}

	//@Test:given/@Before
	var gradleVersion: GradleVersion = GradleVersion.current()
		set(value) {
			val distributionUrl =
				URI.create("https://services.gradle.org/distributions/gradle-${value.version}-all.zip")
			runner
				.withGradleVersion(value.version)
				.withGradleDistribution(distributionUrl)
			field = value
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
		if (path.size == 1 && path[0] == "gradle.properties") {
			propertiesFile.appendText(contents)
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
	fun basedOn(folder: String, relativeTo: Any? = null): GradleRunnerRule =
		@Suppress("DEPRECATION")
		basedOn(templateFile(folder, relativeTo))

	@JvmOverloads
	@Deprecated("This method does not belong on the Gradle rule, use some other utility to load from resources.")
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
