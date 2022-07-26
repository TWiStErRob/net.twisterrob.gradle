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
		@Language("groovy")
		val doNotNag = """
			/**
			 * Surgically ignoring messages like this will prevent actual executions from triggering
			 * stack traces and warnings, which means that even with some warnings,
			 * it's possible to use org.gradle.warning.mode=fail.
			 */
			void doNotNagAbout(String message) {
				java.lang.reflect.Field x = org.gradle.internal.deprecation.DeprecationLogger.class
					.getDeclaredField("DEPRECATED_FEATURE_HANDLER")
				x.setAccessible(true)
				Object logger = x.get(null)
			
				java.lang.reflect.Field y = org.gradle.internal.featurelifecycle.LoggingDeprecatedFeatureHandler.class
						.getDeclaredField("messages")
				y.setAccessible(true)
				Set<String> messages = y.get(logger) as Set<String>

				messages.add(message)
			}
			// net.twisterrob.test.android.pluginVersion=7.0.4
			// net.twisterrob.gradle.runner.gradleVersion=7.4.2
			// > Task :compileDebugRenderscript NO-SOURCE
			doNotNagAbout("Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property sourceDirs with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/7.4.2/userguide/upgrading_version_7.html#empty_directories_file_tree")
			// > Task :compileDebugAidl NO-SOURCE
			doNotNagAbout("Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property sourceFiles with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/7.4.2/userguide/upgrading_version_7.html#empty_directories_file_tree")
			// > Task :stripDebugDebugSymbols NO-SOURCE
			doNotNagAbout("Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property inputFiles with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/7.4.2/userguide/upgrading_version_7.html#empty_directories_file_tree")
			// > Task :bundleLibResDebug NO-SOURCE
			doNotNagAbout("Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property resources with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/7.4.2/userguide/upgrading_version_7.html#empty_directories_file_tree")
			// net.twisterrob.test.android.pluginVersion=4.2.2
			// net.twisterrob.gradle.runner.gradleVersion=7.4.2
			// > Task :mergeDebugNativeLibs NO-SOURCE
			doNotNagAbout("Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property projectNativeLibs with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/7.4.2/userguide/upgrading_version_7.html#empty_directories_file_tree")

			"""
		buildFile.appendText(doNotNag.trimIndent())
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
		val argsWarnings =
			if (gradleVersion < GradleVersion.version("5.6"))
				emptyArray() // "fail" was not a valid option for --warning-mode before Gradle 5.6.
			else
				// https://docs.gradle.org/5.6/release-notes.html#fail-the-build-on-deprecation-warnings
				arrayOf("--warning-mode=fail")
		val args = arrayOf(*tasks, "--stacktrace", *argsWarnings)
		val gradleTestWorkerId: String? by systemProperty(TestWorker.WORKER_ID_SYS_PROPERTY)
		val testKitDir = runner.let { it as? DefaultGradleRunner }?.testKitDirProvider?.dir
		val javaVendor: String? by systemProperty("java.vendor")
		val javaVersion: String? by systemProperty("java.version")
		val javaVersionDate: String? by systemProperty("java.version.date")
		val javaRuntimeName: String? by systemProperty("java.runtime.name")
		val javaRuntimeVersion: String? by systemProperty("java.runtime.version")
		val javaHome: String? by systemProperty("java.home")
		val java = "${javaVendor} ${javaRuntimeName} ${javaVersion} (${javaRuntimeVersion} ${javaVersionDate})"
		println(
			"""
			${gradleVersion} worker #${gradleTestWorkerId} at ${testKitDir?.absolutePath}.
			Java: ${java} from ${javaHome}.
			Gradle properties:
			```properties
${propertiesFileContentForLogging().prependIndent("\t\t\t")}
			```
			Running `gradle ${args.joinToString(" ")}` on ${buildFile.absolutePath}:
			```gradle
${buildFile.readText().prependIndent("\t\t\t")}
			```
			Execution output:
			""".trimIndent()
		)
		return runner.withArguments(*args)
	}

	private fun propertiesFileContentForLogging(): String =
		if (propertiesFile.exists())
			propertiesFile.readText()
		else
			"${propertiesFile.name} does not exist."

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
