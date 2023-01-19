package net.twisterrob.gradle.kotlin

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.test.GradleBuildTestResources
import net.twisterrob.gradle.test.GradleBuildTestResources.basedOn
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoOutputLine
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.test.compile.generateKotlinCompilationCheck
import net.twisterrob.test.compile.generateKotlinCompilationCheckTest
import org.gradle.util.GradleVersion
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.assumeThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @see KotlinPlugin
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class KotlinPluginIntgTest : BaseIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@BeforeEach fun setMemory() {
		// TODEL Workaround for https://github.com/gradle/gradle/issues/23698
		gradle.file("org.gradle.jvmargs=-Xmx512M -XX:MaxMetaspaceSize=384M\n", "gradle.properties")
	}

	@Test fun `can compile Kotlin`() {
		gradle.basedOn(GradleBuildTestResources.kotlin)
		gradle.generateKotlinCompilationCheck()

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.gradle.plugin.kotlin'
		""".trimIndent()

		val result = gradle.run(script, "jar").build()

		result.assertSuccess(":compileKotlin")
	}

	@Test fun `can test Kotlin with TestNG`() {
		gradle.basedOn(GradleBuildTestResources.kotlin)
		gradle.generateKotlinCompilationCheck()
		gradle.generateKotlinCompilationCheckTest()

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.gradle.plugin.kotlin'
			dependencies {
				testImplementation "org.testng:testng:6.14.3"
			}
		""".trimIndent()

		val result = gradle.run(script, "test").build()

		result.assertSuccess(":compileKotlin")
		result.assertSuccess(":compileTestKotlin")
	}

	@Test fun `can test Kotlin with JUnit`() {
		gradle.basedOn(GradleBuildTestResources.kotlin)
		gradle.generateKotlinCompilationCheck()
		gradle.generateKotlinCompilationCheckTest()

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.gradle.plugin.kotlin'
			dependencies {
				testImplementation "junit:junit:4.13.1"
			}
		""".trimIndent()

		val result = gradle.run(script, "test").build()

		result.assertSuccess(":compileKotlin")
		result.assertSuccess(":compileTestKotlin")
	}

	@Test fun `does not add repositories when it would fail`() {
		// See https://docs.gradle.org/6.8/release-notes.html#central-declaration-of-repositories.
		assumeThat(
			"Feature added in Gradle 6.8",
			gradle.gradleVersion.baseVersion,
			greaterThanOrEqualTo(GradleVersion.version("6.8"))
		)

		gradle.basedOn(GradleBuildTestResources.kotlin)
		// Somewhere after Kotlin 1.4.32 and before 1.5.32 there was a behavior change:
		// Not having a source code would trigger compileKotlin to be NO-SOURCE.
		// This means it doesn't try to resolve classpath, so th expected failure would never come.
		gradle.file("", "src", "main", "java", "triggerCompilation.kt")

		@Language("gradle")
		val settings = """
			dependencyResolutionManagement {
				repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
			}
		"""
		gradle.settingsFile.writeText(settings)

		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.gradle.plugin.kotlin'
		""".trimIndent()

		val result = gradle.run(script, "jar").buildAndFail()

		result.assertNoOutputLine(""".*Build was configured to prefer settings repositories over project repositories but repository 'MavenRepo' was added by plugin 'net\.twisterrob\.kotlin'""".toRegex())
		result.assertNoOutputLine(""".*Build was configured to prefer settings repositories over project repositories but repository '.*' was added by plugin '.*'""".toRegex())
		result.assertHasOutputLine(""".*Cannot resolve external dependency (.*) because no repositories are defined\.""".toRegex())
	}

	@Test fun `applying by the old name is deprecated`() {
		if (gradle.gradleVersion.baseVersion < GradleVersion.version("6.3")) {
			val result = gradle.run("apply plugin: 'net.twisterrob.kotlin'").build()
			result.assertHasOutputLine(
				"Plugin net.twisterrob.kotlin is deprecated, " +
						"please use net.twisterrob.gradle.plugin.kotlin instead."
			)
		} else {
			val result = gradle.run("apply plugin: 'net.twisterrob.kotlin'").buildAndFail()
			result.assertHasOutputLine(
				Regex(
					"""org\.gradle\.api\.GradleException: """ +
							"""Deprecated Gradle features were used in this build, making it incompatible with Gradle \d.0"""
				)
			)
			result.assertHasOutputLine(
				Regex(
					"""The net\.twisterrob\.kotlin plugin has been deprecated\. """
							+ """This is scheduled to be removed in Gradle \d\.0\. """
							+ """Please use the net\.twisterrob\.gradle\.plugin\.kotlin plugin instead."""
				)
			)
		}
	}
}
