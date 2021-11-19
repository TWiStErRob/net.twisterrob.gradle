package net.twisterrob.gradle.android.tasks

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.test.GradleBuildTestResources
import net.twisterrob.gradle.test.GradleBuildTestResources.basedOn
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.gradle.test.failReason
import net.twisterrob.gradle.test.root
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.io.FileMatchers.anExistingDirectory
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @see TestReportGenerator
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class TaskReportGeneratorIntgTest : BaseIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	@Test fun `generator runs with empty input`() {
		gradle.basedOn(GradleBuildTestResources.android)
		gradle.root.resolve("my_test_input").mkdirs()

		@Language("gradle")
		val script = """
			//noinspection GroovyAssignabilityCheck
			task generateHtmlReportFromXml(type: ${TestReportGenerator::class.qualifiedName}) {
				input = new File(rootDir, 'my_test_input')
				output = new File(buildDir, 'my_test_results')
				//outputs.upToDateWhen { false }
			}
		""".trimIndent()

		val result = gradle.run(script, "generateHtmlReportFromXml").build()

		result.assertSuccess(":generateHtmlReportFromXml")
		assertThat(gradle.root.resolve("build/my_test_results"), anExistingDirectory())
	}

	@Test fun `missing output`() {
		gradle.basedOn(GradleBuildTestResources.android)

		@Language("gradle")
		val script = """
			//noinspection GroovyAssignabilityCheck
			task generateHtmlReportFromXml(type: ${TestReportGenerator::class.qualifiedName}) {
				input = new File(rootDir, 'subfolder')
			}
		""".trimIndent()

		val result = gradle.run(script, "generateHtmlReportFromXml").buildAndFail()

		assertThat(result.failReason, containsString("lateinit property output has not been initialized"))
	}

	@Test fun `missing input`() {
		gradle.basedOn(GradleBuildTestResources.android)

		@Language("gradle")
		val script = """
			//noinspection GroovyAssignabilityCheck
			task generateHtmlReportFromXml(type: ${TestReportGenerator::class.qualifiedName}) {
				output = new File(rootDir, 'subfolder')
			}
		""".trimIndent()

		val result = gradle.run(script, "generateHtmlReportFromXml").buildAndFail()

		assertThat(result.failReason, containsString("lateinit property input has not been initialized"))
	}
}
