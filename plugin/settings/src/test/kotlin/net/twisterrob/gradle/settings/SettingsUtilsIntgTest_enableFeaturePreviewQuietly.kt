package net.twisterrob.gradle.settings

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoOutputLine
import org.gradle.util.GradleVersion
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.assumeThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * This test is using Gradle specific features, which vary heavily between versions.
 *
 * * [org.gradle.api.internal.FeaturePreviews.Feature.TYPESAFE_PROJECT_ACCESSORS]
 *   was added in [7.0.0](https://github.com/gradle/gradle/commit/9b5c6b67f5526436f111429138f938194d42fc2d).
 * * [org.gradle.api.internal.FeaturePreviews.Feature.GROOVY_COMPILATION_AVOIDANCE]
 *   exists from Gradle 6.1.1 (and probably even earlier) to Gradle 7.5.1 (and probably even further),
 *   but is hard to trigger for testing.
 * @see enableFeaturePreviewQuietly
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class SettingsUtilsIntgTest_enableFeaturePreviewQuietly : BaseIntgTest() {
	override lateinit var gradle: GradleRunnerRule

	@Test fun `outputs info line when applying typesafe accessors`() {
		assumeThat(
			"TYPESAFE_PROJECT_ACCESSORS was added in Gradle 7.0.0.",
			gradle.gradleVersion.baseVersion,
			greaterThanOrEqualTo(GradleVersion.version("7.0"))
		)

		@Language("gradle.kts")
		val settings = """
			rootProject.name = "my-root"
			
			plugins {
				id("net.twisterrob.gradle.plugin.settings")
			}
			
			enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
		""".trimIndent()
		gradle.file(settings, "settings.gradle.kts")

		@Language("gradle")
		val build = """
			// Use type-safe project accessors for a sanity check.
			println("My project: " + projects.myRoot.name)
		""".trimIndent()

		val result = gradle.run(build).build()

		result.assertHasOutputLine("My project: my-root")
		result.assertHasOutputLine("Type-safe project accessors is an incubating feature.")
	}

	@Test fun `quietly suppresses info line when applying typesafe accessors`() {
		assumeThat(
			"TYPESAFE_PROJECT_ACCESSORS was added in Gradle 7.0.0.",
			gradle.gradleVersion.baseVersion,
			greaterThanOrEqualTo(GradleVersion.version("7.0"))
		)

		@Language("gradle.kts")
		val settings = """
			import net.twisterrob.gradle.settings.enableFeaturePreviewQuietly
			
			plugins {
				id("net.twisterrob.gradle.plugin.settings")
			}
			
			rootProject.name = "my-root"
			
			enableFeaturePreviewQuietly("TYPESAFE_PROJECT_ACCESSORS", "Type-safe project accessors")
		""".trimIndent()
		gradle.file(settings, "settings.gradle.kts")

		@Language("gradle")
		val build = """
			// Use type-safe project accessors for a sanity check.
			println("My project: " + projects.myRoot.name)
		""".trimIndent()

		val result = gradle.run(build).build()

		result.assertHasOutputLine("My project: my-root")
		result.assertNoOutputLine("Type-safe project accessors is an incubating feature.")
	}

	@Test fun `quietly suppresses info line when applying typesafe accessors (Groovy)`() {
		assumeThat(
			"TYPESAFE_PROJECT_ACCESSORS was added in Gradle 7.0.0.",
			gradle.gradleVersion.baseVersion,
			greaterThanOrEqualTo(GradleVersion.version("7.0"))
		)

		@Language("gradle")
		val settings = """
			import static net.twisterrob.gradle.settings.SettingsUtils.enableFeaturePreviewQuietly
			
			plugins {
				id("net.twisterrob.gradle.plugin.settings")
			}
			
			rootProject.name = "my-root"
			
			enableFeaturePreviewQuietly(settings, "TYPESAFE_PROJECT_ACCESSORS", "Type-safe project accessors")
		""".trimIndent()
		gradle.file(settings, "settings.gradle")

		@Language("gradle")
		val build = """
			// Use type-safe project accessors for a sanity check.
			println("My project: " + projects.myRoot.name)
		""".trimIndent()

		val result = gradle.run(build).build()

		result.assertHasOutputLine("My project: my-root")
		result.assertNoOutputLine("Type-safe project accessors is an incubating feature.")
	}
}
