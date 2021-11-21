package net.twisterrob.gradle.root

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @see RootPlugin
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class RootPluginIntgTest : BaseIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Test fun `applies GradlePlugin`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.root'
			
			plugins.withType(${GradlePlugin::class.qualifiedName}) {
				println("Gradle Plugin applied")
			}
		""".trimIndent()

		val result = gradle.run(script).build()

		result.assertHasOutputLine("Gradle Plugin applied")
	}
}
