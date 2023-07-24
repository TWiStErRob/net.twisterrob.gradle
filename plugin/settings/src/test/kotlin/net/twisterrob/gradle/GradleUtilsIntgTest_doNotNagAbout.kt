package net.twisterrob.gradle

import net.twisterrob.gradle.internal.deprecation.canNagUser
import net.twisterrob.gradle.internal.deprecation.nextMajorVersion
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoOutputLine
import net.twisterrob.gradle.test.fixtures.ContentMergeMode
import net.twisterrob.gradle.test.runBuild
import net.twisterrob.gradle.test.runFailingBuild
import org.gradle.testkit.runner.BuildResult
import org.gradle.util.GradleVersion
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.lessThan
import org.hamcrest.assumeThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junitpioneer.jupiter.Issue

/**
 * @see doNotNagAbout
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class GradleUtilsIntgTest_doNotNagAbout : BaseIntgTest() {
	override lateinit var gradle: GradleRunnerRule

	@BeforeEach fun applySettingsPlugin() {
		@Language("gradle")
		val settings = """
			plugins {
				id("net.twisterrob.gradle.plugin.settings")
			}
		""".trimIndent()
		gradle.file(settings, ContentMergeMode.MERGE_GRADLE, "settings.gradle.kts")
	}

	@Test fun `ensure that nagging works`() {
		assumeTrue(canNagUser(gradle.gradleVersion))

		val script = """
			${nag("""Fake nagging for test""")}
		""".trimIndent()
		val result = gradle.runFailingBuild {
			run(script)
		}

		result.verifyNagging("Fake nagging for test", 7)
	}

	@Test fun `disable nagging for specific feature used in configuration phase`() {
		assumeTrue(canNagUser(gradle.gradleVersion))

		val buildFileLine =
			if (GradleVersion.version("8.0") <= gradle.gradleVersion.baseVersion) {
				"""
					"Build file '${'$'}{buildFile.absolutePath}': line 11${'$'}{System.lineSeparator()}" +
				""".trimIndent()
			} else {
				""
			}
		val gradleVersion = nextMajorVersion(gradle.gradleVersion)
		@Suppress("UnnecessaryQualifiedReference")
		val script = """
			net.twisterrob.gradle.GradleUtils.doNotNagAbout(
				${buildFileLine}
				"Fake nagging for test has been deprecated. This is scheduled to be removed in ${gradleVersion}."
			)
			${nag("""Fake nagging for test""")}
					
		""".trimIndent()
		val result = gradle.runBuild {
			run(script)
		}

		result.assertNoOutputLine(Regex(""".*Fake nagging for test.*"""))
	}

	@Test fun `disable nagging for specific feature used in execution phase`() {
		assumeTrue(canNagUser(gradle.gradleVersion))

		val buildFileLine =
			if (GradleVersion.version("8.0") <= gradle.gradleVersion.baseVersion) {
				"""
					"Build file '${'$'}{buildFile.absolutePath}': line 13${'$'}{System.lineSeparator()}" +
				""".trimIndent()
			} else {
				""
			}
		val gradleVersion = nextMajorVersion(gradle.gradleVersion)
		@Suppress("UnnecessaryQualifiedReference")
		val script = """
			net.twisterrob.gradle.GradleUtils.doNotNagAbout(
				${buildFileLine}
				"Fake nagging for test has been deprecated. This is scheduled to be removed in ${gradleVersion}."
			)
			tasks.register("nag") {
				doLast {
					${nag("""Fake nagging for test""")}
				}
			}
		""".trimIndent()
		val result = gradle.runBuild {
			run(script, "nag")
		}

		result.assertNoOutputLine(Regex(""".*Fake nagging for test.*"""))
	}

	@Test fun `deprecation can not be suppressed with stack trace`() {
		assumeThat(gradle.gradleVersion.baseVersion, lessThan(GradleVersion.version("8.0")))

		@Suppress("UnnecessaryQualifiedReference")
		val script = """
			net.twisterrob.gradle.GradleUtils.doNotNagAbout(
				"Something",
				"at thing"
			)
		""".trimIndent()

		val result = gradle.runFailingBuild {
			run(script)
		}

		result.assertHasOutputLine("> Stack traces for deprecations are not available in ${gradle.gradleVersion}.")
	}

	@Test fun `deprecation can be suppressed with stack trace`() {
		assumeThat(gradle.gradleVersion.baseVersion, greaterThanOrEqualTo(GradleVersion.version("8.0")))

		val gradleVersion = nextMajorVersion(gradle.gradleVersion)
		@Suppress("UnnecessaryQualifiedReference")
		val script = """
			net.twisterrob.gradle.GradleUtils.doNotNagAbout(
				"Build file '${'$'}{buildFile.absolutePath}': line 12${'$'}{System.lineSeparator()}" +
				"Fake nagging for test has been deprecated. This is scheduled to be removed in ${gradleVersion}.",
				"build.gradle:12" // specific line in generated build.gradle file
			)
			${nag("""Fake nagging for test""")}
		""".trimIndent()
		val result = gradle.runBuild {
			run(script, "--stacktrace")
		}

		result.assertNoOutputLine(Regex(""".*Fake nagging for test.*"""))
		result.assertNoOutputLine("Build file '${gradle.buildFile.absolutePath}': line 12")
	}

	@Test fun `deprecation can be suppressed with stack trace (specific instance)`() {
		assumeThat(gradle.gradleVersion.baseVersion, greaterThanOrEqualTo(GradleVersion.version("8.0")))

		val gradleVersion = nextMajorVersion(gradle.gradleVersion)
		@Suppress("UnnecessaryQualifiedReference")
		val script = """
			net.twisterrob.gradle.GradleUtils.doNotNagAbout(
				"Build file '${'$'}{buildFile.absolutePath}': line 12${'$'}{System.lineSeparator()}" +
				"Fake nagging for test has been deprecated. This is scheduled to be removed in ${gradleVersion}.",
				"build.gradle:12" // specific line in generated build.gradle file
			)
			${nag("""Fake nagging for test""")}
			${nag("""Fake nagging for test""")}
		""".trimIndent()
		val result = gradle.runFailingBuild {
			run(script, "--stacktrace")
		}

		result.assertNoOutputLine("Build file '${gradle.buildFile.absolutePath}': line 12")
		result.assertHasOutputLine("Build file '${gradle.buildFile.absolutePath}': line 20")
		result.verifyNagging("Fake nagging for test", 20)
	}

	/**
	 * This test is meant to show deprecation nagging in their full glory.
	 */
	@Issue("https://github.com/gradle/gradle/issues/25872")
	@Test fun `stack traces are visible after many nags`() {
		val script = """
			apply plugin: 'java'
			// Do something that triggers many deprecation nags.
			for (int i in 1..50) {
				// Uses Conventions which are deprecated and nag.
				// Uses BasePluginConvention.setArchivesBaseName(String) which is deprecated and nags.
				archivesBaseName = 'trigger-nagging' // line 6
			}
			// ClosureBackedAction type is deprecated, nagging is in the class initializer.
			//noinspection GrDeprecatedAPIUsage,UnnecessaryQualifiedReference
			org.gradle.util.ClosureBackedAction.of {} // line 10
		""".trimIndent()
		val result = gradle.runBuild {
			run(script, "--warning-mode=all")
		}

		result.assertHasOutputLine(
			"""The org.gradle.api.plugins.BasePluginConvention type has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/${gradle.gradleVersion.version}/userguide/upgrading_version_8.html#base_convention_deprecation"""
		)
		result.assertHasOutputLine(Regex("""Build file '\Q${gradle.buildFile.absolutePath}\E': line 6"""))
		result.assertHasOutputLine(Regex("""\tat build_[a-z0-9]+\.run\(\Q${gradle.buildFile.absolutePath}\E:6\)"""))
		result.assertHasOutputLine(
			"""The org.gradle.util.ClosureBackedAction type has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/${gradle.gradleVersion.version}/userguide/upgrading_version_7.html#org_gradle_util_reports_deprecations"""
		)
		result.assertHasOutputLine(Regex("""Build file '\Q${gradle.buildFile.absolutePath}\E': line 10"""))
		result.assertHasOutputLine(Regex("""\tat build_[a-z0-9]+\.run\(\Q${gradle.buildFile.absolutePath}\E:10\)"""))
	}

	private fun BuildResult.verifyNagging(feature: String, line: Int) {
		val gradleVersionRegex = Regex.escape(nextMajorVersion(gradle.gradleVersion).toString())
		assertHasOutputLine(
			Regex(
				"""
					> Configure project :\r?
					Build file '${gradle.buildFile.absolutePath.replace("\\", "\\\\")}': line ${line}\r?
					${feature} has been deprecated. This is scheduled to be removed in ${gradleVersionRegex}.
				""".trimIndent()
			)
		)
		assertHasOutputLine(
			Regex(
				"""
					\* What went wrong:\r?
					Deprecated Gradle features were used in this build, making it incompatible with ${gradleVersionRegex}
				""".trimIndent()
			)
		)
	}

	companion object {

		private var counter = 0

		@Language("gradle")
		private fun nag(feature: String): String {
			val varName = "builder${counter++}"
			// Note: on older Gradle versions (e.g. 6.5.1) GradleVersion is not auto-imported.
			@Suppress("UnnecessaryQualifiedReference")
			return """
				// Create a fake deprecation warning for "${feature}" in the Gradle internals.
							def ${varName} = org.gradle.internal.deprecation.DeprecationLogger
									.deprecate("${feature}")
							net.twisterrob.gradle.internal.deprecation.DeprecationUtils
									.willBeRemovedInGradleNextMajor(${varName}, org.gradle.util.GradleVersion.current())
							// Passing in DeprecationUtils for calledFrom, so anything above it will be truncated in stack.
							net.twisterrob.gradle.internal.deprecation.DeprecationUtils
									.nagUserWith(${varName}, net.twisterrob.gradle.internal.deprecation.DeprecationUtils.class)
			""".trimIndent() // Align with usages, first line will be indented already.
		}
	}
}
