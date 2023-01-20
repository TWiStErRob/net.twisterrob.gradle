package net.twisterrob.gradle

import net.twisterrob.gradle.internal.deprecation.canNagUser
import net.twisterrob.gradle.internal.deprecation.nextMajorVersion
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoOutputLine
import net.twisterrob.gradle.test.runBuild
import net.twisterrob.gradle.test.runFailingBuild
import org.gradle.testkit.runner.BuildResult
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @see doNotNagAbout
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class GradleUtilsTest_doNotNagAbout : BaseIntgTest() {
	override lateinit var gradle: GradleRunnerRule

	@Test fun `ensure that nagging works`() {
		assumeTrue(canNagUser(gradle.gradleVersion))

		val script = """
			${nag("""Fake nagging for test""")}
		""".trimIndent()
		val result = gradle.runFailingBuild {
			run(script)
		}

		result.verifyNagging("Fake nagging for test")
	}

	@Test fun `disable nagging for specific feature`() {
		assumeTrue(canNagUser(gradle.gradleVersion))

		val gradleVersion = nextMajorVersion(gradle.gradleVersion)
		@Suppress("UnnecessaryQualifiedReference")
		val script = """
			net.twisterrob.gradle.GradleUtils.doNotNagAbout(
				"Fake nagging for test has been deprecated. This is scheduled to be removed in ${gradleVersion}."
			)
			${nag("""Fake nagging for test""")}
					
		""".trimIndent()
		val result = gradle.runBuild {
			run(script)
		}

		result.assertNoOutputLine(Regex(""".*Fake nagging for test.*"""))
	}

	@Test fun `only suppresses the specific feature (prefix)`() {
		assumeTrue(canNagUser(gradle.gradleVersion))

		val gradleVersion = nextMajorVersion(gradle.gradleVersion)
		@Suppress("UnnecessaryQualifiedReference")
		val script = """
			net.twisterrob.gradle.GradleUtils.doNotNagAbout(
				"Fake nagging for test has been deprecated. This is scheduled to be removed in ${gradleVersion}."
			)
			${nag("""Fake nagging for test""")}
			// Slightly different message that contains the suppressed one.
			${nag("""Important Fake nagging for test""")}
		""".trimIndent()
		val result = gradle.runFailingBuild {
			run(script)
		}

		result.verifyNagging("Important Fake nagging for test")
		result.assertNoOutputLine(Regex(""".*(?<!Important )Fake nagging for test.*"""))
	}

	@Test fun `only suppresses the specific feature (suffix)`() {
		assumeTrue(canNagUser(gradle.gradleVersion))

		val gradleVersion = nextMajorVersion(gradle.gradleVersion)
		@Suppress("UnnecessaryQualifiedReference")
		val script = """
			net.twisterrob.gradle.GradleUtils.doNotNagAbout(
				"Fake nagging for test has been deprecated. This is scheduled to be removed in ${gradleVersion}."
			)
			${nag("""Fake nagging for test""")}
			// Slightly different message that contains the suppressed one.
			${nag("""Fake nagging for test 2""")}
		""".trimIndent()
		val result = gradle.runFailingBuild {
			run(script)
		}

		result.verifyNagging("Fake nagging for test 2")
		result.assertNoOutputLine(Regex(""".*Fake nagging for test(?! 2).*"""))
	}

	private fun BuildResult.verifyNagging(feature: String) {
		val gradleVersionRegex = Regex.escape(nextMajorVersion(gradle.gradleVersion).toString())
		assertHasOutputLine(
			Regex(
				"""
					> Configure project :\r?
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

		@Language("gradle")
		private fun nag(feature: String): String {
			val varName = "builder${Integer.toHexString(feature.hashCode())}"
			// Note: on older Gradles (e.g. 6.5.1) GradleVersion is not auto-imported.
			@Suppress("UnnecessaryQualifiedReference")
			return """
				// Create a fake deprecation warning for "${feature}" in the Gradle internals.
							def ${varName} = org.gradle.internal.deprecation.DeprecationLogger
									.deprecate("${feature}")
							net.twisterrob.gradle.internal.deprecation.DeprecationUtils
									.willBeRemovedInGradleNextMajor(${varName}, org.gradle.util.GradleVersion.current())
							net.twisterrob.gradle.internal.deprecation.DeprecationUtils
									.nagUserWith(${varName}, this.class)
			""".trimIndent() // Align with usages, first line will be indented already.
		}
	}
}
