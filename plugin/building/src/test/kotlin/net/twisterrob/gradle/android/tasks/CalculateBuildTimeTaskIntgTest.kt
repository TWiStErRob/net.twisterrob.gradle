package net.twisterrob.gradle.android.tasks

import net.twisterrob.gradle.android.BaseAndroidIntgTest
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.gradle.test.assertUpToDate
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @see CalculateBuildTimeTask
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class CalculateBuildTimeTaskIntgTest : BaseAndroidIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Test fun `will stay up to date`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.gradle.plugin.vcs'
			tasks.register("calculateBuildConfigBuildTime", ${CalculateBuildTimeTask::class.java.name})
		""".trimIndent()

		val first = gradle.run(script, "calculateBuildConfigBuildTime").build()
		first.assertSuccess(":calculateBuildConfigBuildTime")

		val second = gradle.run(null, "calculateBuildConfigBuildTime").build()
		second.assertUpToDate(":calculateBuildConfigBuildTime")
	}

	@Test fun `will stay up to date with custom value`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.gradle.plugin.vcs'
			tasks.register("calculateBuildConfigBuildTime", ${CalculateBuildTimeTask::class.java.name}) {
				buildTime.set(1234L)
			}
		""".trimIndent()

		val first = gradle.run(script, "calculateBuildConfigBuildTime").build()
		first.assertSuccess(":calculateBuildConfigBuildTime")

		val second = gradle.run(null, "calculateBuildConfigBuildTime").build()
		second.assertUpToDate(":calculateBuildConfigBuildTime")
	}
}
