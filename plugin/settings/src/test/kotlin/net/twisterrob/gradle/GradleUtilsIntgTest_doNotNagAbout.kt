package net.twisterrob.gradle

import net.twisterrob.gradle.internal.deprecation.canNagUser
import net.twisterrob.gradle.internal.deprecation.nextMajorVersion
import net.twisterrob.gradle.test.ContentMergeMode
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoOutputLine
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

	@Test fun `disable nagging for specific feature`() {
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
