package net.twisterrob.gradle

import net.twisterrob.gradle.internal.deprecation.canNagUser
import net.twisterrob.gradle.internal.deprecation.nextMajorVersionForDeprecation
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
 * @see net.twisterrob.gradle.nagging.NaggingPlugin
 * @see doNotNagAbout
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class GradleUtilsIntgTest_doNotNagAbout : BaseIntgTest() {
	override lateinit var gradle: GradleRunnerRule

	@BeforeEach fun applyPlugin() {
		@Language("gradle")
		val settings = """
			plugins {
				id("net.twisterrob.gradle.plugin.nagging")
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
		@Suppress("UnnecessaryQualifiedReference")
		val script = """
			net.twisterrob.gradle.GradleUtils.doNotNagAbout(
				${buildFileLine}
				"Fake nagging for test has been deprecated. This is scheduled to be removed in ${gradle.nextMajor}."
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
		@Suppress("UnnecessaryQualifiedReference")
		val script = """
			net.twisterrob.gradle.GradleUtils.doNotNagAbout(
				${buildFileLine}
				"Fake nagging for test has been deprecated. This is scheduled to be removed in ${gradle.nextMajor}."
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

		@Suppress("UnnecessaryQualifiedReference")
		val script = """
			net.twisterrob.gradle.GradleUtils.doNotNagAbout(
				"Build file '${'$'}{buildFile.absolutePath}': line 12${'$'}{System.lineSeparator()}" +
				"Fake nagging for test has been deprecated. This is scheduled to be removed in ${gradle.nextMajor}.",
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

		@Suppress("UnnecessaryQualifiedReference")
		val script = """
			net.twisterrob.gradle.GradleUtils.doNotNagAbout(
				"Build file '${'$'}{buildFile.absolutePath}': line 12${'$'}{System.lineSeparator()}" +
				"Fake nagging for test has been deprecated. This is scheduled to be removed in ${gradle.nextMajor}.",
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
			apply plugin: "org.gradle.java"
			// Do something that triggers many deprecation nags.
			for (int i in 1..1000) {
				${nagManyTimes().prependIndent("\t\t\t\t").trimStart()}
			}
			${nagOnce().prependIndent("\t\t\t").trimStart()}
		""".trimIndent()

		val result = gradle.runBuild {
			run(script, "--warning-mode=all")
		}

		result.verifyNagManyTimes()
		result.verifyNagOnce()
	}

	private fun nagManyTimes(): String =
		when {
			GradleVersion.version("8.14") <= gradle.gradleVersion.baseVersion -> {
				"""
					//noinspection GrDeprecatedAPIUsage
					gradle.startParameter.configurationCacheRequested
				""".trimIndent()
			}
			GradleVersion.version("7.0") <= gradle.gradleVersion.baseVersion -> {
				// Removed in 9.0
				"""
					//noinspection GrDeprecatedAPIUsage
					repositories.jcenter()
				""".trimIndent()
			}
			else -> {
				error("Cannot nag many times for ${gradle.gradleVersion}")
			}
		}

	private fun BuildResult.verifyNagManyTimes() {
		when {
			GradleVersion.version("8.14") <= gradle.gradleVersion.baseVersion -> {
				val gradle10 = nextMajorVersionForDeprecation(GradleVersion.version("9.0"), gradle.gradleVersion)
				assertHasOutputLine(
					"The StartParameter.isConfigurationCacheRequested property has been deprecated. " +
							"This is scheduled to be removed in ${gradle10}. " +
							"Please use 'configurationCache.requested' property on 'BuildFeatures' service instead. " +
							"Consult the upgrading guide for further information: " +
							"https://docs.gradle.org/${gradle.gradleVersion.version}/userguide/upgrading_version_8.html" +
							"#deprecated_startparameter_is_configuration_cache_requested"
				)
				assertHasOutputLine(Regex("""\tat build_[a-z0-9]+\.run\(\Q${gradle.buildFile.absolutePath}\E:5\)"""))
			}
			GradleVersion.version("8.0") <= gradle.gradleVersion.baseVersion -> {
				assertHasOutputLine(
					"The RepositoryHandler.jcenter() method has been deprecated. " +
							"This is scheduled to be removed in Gradle 9.0. " +
							"JFrog announced JCenter's sunset in February 2021. " +
							"Use mavenCentral() instead. " +
							"Consult the upgrading guide for further information: " +
							"https://docs.gradle.org/${gradle.gradleVersion.version}/userguide/upgrading_version_6.html" +
							"#jcenter_deprecation"
				)
				assertHasOutputLine(Regex("""\tat build_[a-z0-9]+\.run\(\Q${gradle.buildFile.absolutePath}\E:5\)"""))
			}
			GradleVersion.version("7.2") <= gradle.gradleVersion.baseVersion -> {
				assertHasOutputLine(
					"The RepositoryHandler.jcenter() method has been deprecated. " +
							"This is scheduled to be removed in Gradle 8.0. " +
							"JFrog announced JCenter's sunset in February 2021. " +
							"Use mavenCentral() instead. " +
							"Consult the upgrading guide for further information: " +
							"https://docs.gradle.org/${gradle.gradleVersion.version}/userguide/upgrading_version_6.html" +
							"#jcenter_deprecation"
				)
				assertHasOutputLine(Regex("""\tat build_[a-z0-9]+\.run\(\Q${gradle.buildFile.absolutePath}\E:5\)"""))
			}
			GradleVersion.version("7.0") <= gradle.gradleVersion.baseVersion -> {
				assertHasOutputLine(
					"The RepositoryHandler.jcenter() method has been deprecated. " +
							"This is scheduled to be removed in Gradle 8.0. " +
							"JFrog announced JCenter's shutdown in February 2021. " +
							"Use mavenCentral() instead. " +
							"Consult the upgrading guide for further information: " +
							"https://docs.gradle.org/${gradle.gradleVersion.version}/userguide/upgrading_version_6.html" +
							"#jcenter_deprecation"
				)
				assertHasOutputLine(Regex("""\tat build_[a-z0-9]+\.run\(\Q${gradle.buildFile.absolutePath}\E:5\)"""))
			}
			else -> {
				error("Cannot nag many times for ${gradle.gradleVersion}")
			}
		}
	}

	private fun nagOnce(): String =
		when {
			GradleVersion.version("9.0.0") <= gradle.gradleVersion.baseVersion -> {
				"""
					// This Feature will always be inactive and hence nag about removal in next major.
					// See org.gradle.initialization.DefaultSettings.enableFeaturePreview for nagging code.
					//noinspection UnnecessaryQualifiedReference
					(gradle as org.gradle.api.internal.GradleInternal).settings.enableFeaturePreview("ALWAYS_INACTIVE")
				""".trimIndent()
			}
			GradleVersion.version("8.0") <= gradle.gradleVersion.baseVersion -> {
				"""
					// ClosureBackedAction type is deprecated, nagging is in the class initializer.
					//noinspection GrDeprecatedAPIUsage,UnnecessaryQualifiedReference
					org.gradle.util.ClosureBackedAction.of {}
				""".trimIndent()
			}
			GradleVersion.version("7.0") <= gradle.gradleVersion.baseVersion -> {
				"""
					// DefaultDomainObjectSet constructor taking a class is deprecated, nagging is in the constructor.
					//noinspection GrDeprecatedAPIUsage,UnnecessaryQualifiedReference
					new org.gradle.api.internal.DefaultDomainObjectSet(String.class)
				""".trimIndent()
			}
			else -> {
				error("Cannot nag once for ${gradle.gradleVersion}")
			}
		}

	private fun BuildResult.verifyNagOnce() {
		when {
			GradleVersion.version("9.0.0") <= gradle.gradleVersion.baseVersion -> {
				assertHasOutputLine(
					"enableFeaturePreview('ALWAYS_INACTIVE') has been deprecated. " +
							"This is scheduled to be removed in Gradle 10. " +
							"The feature flag is no longer relevant, please remove it from your settings file. " +
							"For more information, please refer to " +
							"https://docs.gradle.org/${gradle.gradleVersion.version}/userguide/feature_lifecycle.html#feature_preview" +
							" in the Gradle documentation."
				)
				assertHasOutputLine(Regex("""\tat build_[a-z0-9]+\.run\(\Q${gradle.buildFile.absolutePath}\E:10\)"""))
			}
			GradleVersion.version("8.0") <= gradle.gradleVersion.baseVersion -> {
				assertHasOutputLine(
					"The org.gradle.util.ClosureBackedAction type has been deprecated. " +
							"This is scheduled to be removed in Gradle 9.0. " +
							"Consult the upgrading guide for further information: " +
							"https://docs.gradle.org/${gradle.gradleVersion.version}/userguide/upgrading_version_7.html" +
							"#org_gradle_util_reports_deprecations"
				)
				assertHasOutputLine(Regex("""\tat build_[a-z0-9]+\.run\(\Q${gradle.buildFile.absolutePath}\E:9\)"""))
			}
			GradleVersion.version("7.0") <= gradle.gradleVersion.baseVersion -> {
				assertHasOutputLine(
					"Internal API constructor DefaultDomainObjectSet(Class<T>) has been deprecated. " +
							"This is scheduled to be removed in Gradle 8.0. " +
							"Please use ObjectFactory.domainObjectSet(Class<T>) instead. " +
							"See https://docs.gradle.org/${gradle.gradleVersion.version}/userguide/custom_gradle_types.html" +
							"#domainobjectset for more details."
				)
				assertHasOutputLine(Regex("""\tat build_[a-z0-9]+\.run\(\Q${gradle.buildFile.absolutePath}\E:9\)"""))
			}
			else -> {
				error("Cannot nag once for ${gradle.gradleVersion}")
			}
		}
	}

	private fun BuildResult.verifyNagging(feature: String, line: Int) {
		val gradleVersionRegex = Regex.escape(gradle.nextMajor)
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

		private val GradleRunnerRule.nextMajor: String
			get() = nextMajorVersionForDeprecation(gradleVersion, gradleVersion)
	}
}
