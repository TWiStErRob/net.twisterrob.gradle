package net.twisterrob.gradle.settings

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoOutputLine
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(GradleRunnerRuleExtension::class)
class SettingsPluginIntgTest : BaseIntgTest() {
	override lateinit var gradle: GradleRunnerRule

	@Test fun `outputs info line when applying typesafe accessors`() {
		@Language("gradle")
		val settings = """
			rootProject.name = "my-root"
			
			plugins {
				id("net.twisterrob.settings")
			}
			
			enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
		""".trimIndent()
		gradle.file(settings, "settings.gradle.kts")

		@Language("gradle")
		val build = """
			// Use type-safe project accessors for a sanity check.
			println("My project: " + projects.myRoot.dependencyProject.path)
		""".trimIndent()

		val result = gradle.run(build).build()

		result.assertHasOutputLine("My project: :")
		result.assertHasOutputLine("Type-safe project accessors is an incubating feature.")
	}

	@Test fun `quietly suppresses info line when applying typesafe accessors`() {
		@Language("gradle")
		val settings = """
			import net.twisterrob.gradle.settings.enableFeaturePreviewQuietly
			
			plugins {
				id("net.twisterrob.settings")
			}
			
			rootProject.name = "my-root"
			
			enableFeaturePreviewQuietly("TYPESAFE_PROJECT_ACCESSORS", "Type-safe project accessors")
		""".trimIndent()
		gradle.file(settings, "settings.gradle.kts")

		@Language("gradle")
		val build = """
			// Use type-safe project accessors for a sanity check.
			println("My project: " + projects.myRoot.dependencyProject.path)
		""".trimIndent()

		val result = gradle.run(build).build()

		result.assertHasOutputLine("My project: :")
		result.assertNoOutputLine("Type-safe project accessors is an incubating feature.")
	}
}
