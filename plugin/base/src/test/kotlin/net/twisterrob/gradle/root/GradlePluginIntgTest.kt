package net.twisterrob.gradle.root

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertNoSource
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.gradle.test.assertUpToDate
import net.twisterrob.gradle.test.root
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.not
import org.hamcrest.io.FileMatchers.anExistingFile
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @see GradlePlugin
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class GradlePluginIntgTest : BaseIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Test fun `skips debugWrapper if gradlew does not exist`() {
		assertThat(gradle.root.resolve("gradlew.bat"), not(anExistingFile()))
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.root")
			}
		""".trimIndent()

		val result = gradle.run(script, "debugWrapper").build()

		result.assertNoSource(":debugWrapper")
	}

	@Test fun `generates gradled if gradlew exists`() {
		gradle.file("", "gradlew.bat")
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.root")
			}
		""".trimIndent()

		val result = gradle.run(script, "debugWrapper").build()

		result.assertSuccess(":debugWrapper")
		assertThat(gradle.root.resolve("gradled.bat"), anExistingFile())
	}

	@Test fun `debugWrapper is incremental`() {
		gradle.file("", "gradlew.bat")
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.root")
			}
		""".trimIndent()

		gradle.run(script, "debugWrapper").build()
		assertThat(gradle.root.resolve("gradled.bat"), anExistingFile())
		val result = gradle.run(null, "debugWrapper").build()

		result.assertUpToDate(":debugWrapper")
	}
}
