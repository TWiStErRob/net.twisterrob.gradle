package net.twisterrob.gradle

import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.fixtures.ContentMergeMode
import net.twisterrob.gradle.test.runBuild
import net.twisterrob.gradle.test.runFailingBuild
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @see buildFinishedCompat
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class GradleUtilsIntgTest_buildFinishedCompat : BaseIntgTest() {
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

	@Test fun `shows success (groovy)`() {
		val script = """
			net.twisterrob.gradle.GradleUtils.buildFinishedCompat(gradle) { println("Build ended: ${'$'}it") }
		""".trimIndent()

		val result = gradle.runBuild {
			run(script)
		}

		result.assertHasOutputLine("Build ended: null")
	}

	@Test fun `shows failure (groovy)`() {
		val script = """
			net.twisterrob.gradle.GradleUtils.buildFinishedCompat(gradle) { println("Build ended: ${'$'}it") }
			throw new IOException("fail")
		""".trimIndent()

		val result = gradle.runFailingBuild {
			run(script)
		}

		val locationAwareException = "org.gradle.internal.exceptions.LocationAwareException"
		result.assertHasOutputLine(
			"Build ended: ${locationAwareException}: Build file '${gradle.buildFile.absolutePath}' line: 2"
		)
	}
}
