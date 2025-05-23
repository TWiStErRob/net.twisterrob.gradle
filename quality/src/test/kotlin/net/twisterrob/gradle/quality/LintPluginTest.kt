package net.twisterrob.gradle.quality

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertFailed
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoOutputLine
import net.twisterrob.gradle.test.assertNoTask
import net.twisterrob.gradle.test.assertSkipped
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.gradle.test.assertUpToDate
import net.twisterrob.gradle.test.runBuild
import net.twisterrob.gradle.test.runFailingBuild
import org.gradle.testkit.runner.BuildResult
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItems
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import javax.annotation.CheckReturnValue

/**
 * @see LintPlugin
 * @see net.twisterrob.gradle.quality.tasks.GlobalLintGlobalFinalizerTask
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class LintPluginTest : BaseIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	/**
	 * In AGP 7 they removed the Global lint task concept, every lint task analyses a specific variant.
	 * This means that violations are counted double from AGP 7 onwards.
	 * This is counteracted in [net.twisterrob.gradle.quality.report.html.deduplicate] by merging duplicate lints.
	 */
	private val variantMultiplier: Int = 2

	@Test fun `passes when no lint violations found`() {
		val modules: Array<String> = arrayOf(
			"module1",
			"module2",
			"module3"
		)
		modules.forEach { module ->
			@Language("gradle")
			val subProject = """
				plugins {
					id("com.android.library")
				}
				android.namespace = "project.${module}"
				//android.lint.checkOnly = [] // nothing
			""".trimIndent()

			gradle.file(subProject, module, "build.gradle")
			gradle.settingsFile.appendText("""include(":${module}")${System.lineSeparator()}""")
		}

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.quality")
			}
		""".trimIndent()

		val result = gradle.runBuild {
			basedOn("android-multi_module")
			run(script, "lint")
		}

		val lintTasks = result.tasks.map { it.path }.filter { it.endsWith(":lint") }
		assertThat(lintTasks, hasItems(":module1:lint", ":module2:lint", ":module3:lint"))
		assertThat(lintTasks.last(), equalTo(":lint"))
		result.assertSuccess(":module1:lint")
		result.assertSuccess(":module2:lint")
		result.assertSuccess(":module3:lint")
		result.assertSuccess(":lint")
		result.assertNoOutputLine(Regex(""".*Ran lint on subprojects.*"""))
		result.assertNoOutputLine(Regex(""".*See reports in subprojects.*"""))
	}

	@Test fun `gathers results from submodules`() {
		`set up 3 modules with a lint failures`()

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.quality")
			}
		""".trimIndent()

		val result = gradle.runBuild {
			basedOn("android-multi_module")
			run(script, "lint")
		}

		val lintTasks = result.tasks.map { it.path }.filter { it.endsWith(":lint") }
		assertThat(lintTasks, hasItems(":module1:lint", ":module2:lint", ":module3:lint"))
		assertThat(lintTasks.last(), equalTo(":lint"))
		result.assertSuccess(":module1:lint")
		result.assertSuccess(":module2:lint")
		result.assertSuccess(":module3:lint")
		result.assertSuccess(":lint")
		result.assertHasOutputLine(Regex("""Ran lint on subprojects: ${(1 + 1 + 1) * variantMultiplier} issues found\."""))
	}

	@Test fun `does not suggest violation report if already executing it`() {
		`set up 3 modules with a lint failures`()
		gradle.basedOn("android-multi_module")

		@Suppress("detekt.MaxLineLength")
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.quality")
			}
		""".trimIndent()

		val message =
			Regex("""To get a full breakdown and listing, execute violationReportConsole or violationReportHtml\.""")

		gradle.run(script, "lint").build()
			.assertHasOutputLine(message)

		gradle.run(null, "lint", "violationReportHtml").build()
			.assertNoOutputLine(message)

		gradle.run(null, "lint", "violationReportConsole").build()
			.assertNoOutputLine(message)

		// Test also that short names are considered.
		gradle.run(null, "lint", "vRH", "vRC").build()
			.assertNoOutputLine(message)
	}

	@Test fun `ignores disabled submodule lint tasks (rootProject setup)`() {
		// REPORT Since AGP 7.1 "lintRelease" seems to be not executed when calling lint.
		// lintReportRelease executes because of :lint depending on the artifact.
		val disabledTasks = listOf("lintDebug", "lintReportDebug", "lintReportRelease")
		val result = `ignores disabled submodule lint tasks` { buildScript ->
			buildScript + System.lineSeparator() + disabledTasks.joinToString(separator = System.lineSeparator()) { taskName ->
				"evaluationDependsOn(':module2').tasks.getByName('${taskName}').enabled = false"
			}
		}
		disabledTasks.forEach { taskName ->
			result.assertSkipped(":module2:${taskName}")
		}
		result.assertHasOutputLine(Regex("""Ran lint on subprojects: ${(1 + 0 + 1) * variantMultiplier} issues found\."""))
	}

	@Test fun `ignores disabled submodule lint tasks (direct setup)`() {
		val result = `ignores disabled submodule lint tasks` { buildScript ->
			val build2 = gradle.buildFile.parentFile.resolve("module2/build.gradle")
			build2.appendText(System.lineSeparator())
			build2.appendText(
				"""
					afterEvaluate {
						// Tasks are created after on androidComponents.onVariants { }
						tasks.lintDebug.enabled = false
						tasks.lintReportDebug.enabled = false
						tasks.lintRelease.enabled = false
						tasks.lintReportRelease.enabled = false
					}
				""".trimIndent()
			)
			buildScript
		}
		result.assertUpToDate(":module2:lint")
		result.assertSkipped(":module2:lintDebug")
		result.assertNoTask(":module2:lintRelease")
		result.assertHasOutputLine(Regex("""Ran lint on subprojects: ${(1 + 0 + 1) * variantMultiplier} issues found\."""))
	}

	@Test fun `ignores disabled variants (direct setup)`() {
		val result = `ignores disabled submodule lint tasks` { buildScript ->
			val build2 = gradle.buildFile.parentFile.resolve("module2/build.gradle")
			build2.appendText(System.lineSeparator())
			build2.appendText(
				"""
					androidComponents {
						beforeVariants(selector().all()) { enabled = false }
					}
				""".trimIndent()
			)
			buildScript
		}
		result.assertUpToDate(":module2:lint")
		result.assertNoTask(":module2:lintDebug")
		result.assertNoTask(":module2:lintRelease")
		result.assertHasOutputLine(Regex("""Ran lint on subprojects: ${(1 + 0 + 1) * variantMultiplier} issues found\."""))
	}

	@CheckReturnValue
	private fun `ignores disabled submodule lint tasks`(
		extraSetup: (/*@Language("gradle")*/ String) -> /*@Language("gradle")*/ String
	): BuildResult {
		`set up 3 modules with a lint failures`()

		@Language("gradle")
		var script = """
			plugins {
				id("net.twisterrob.gradle.plugin.quality")
			}
		""".trimIndent()

		script = extraSetup(script)

		val result = gradle.runBuild {
			basedOn("android-multi_module")
			run(script, "lint")
		}

		val lintTasks = result.tasks.map { it.path }.filter { it.endsWith(":lint") }
		assertThat(lintTasks, hasItems(":module1:lint", ":module2:lint", ":module3:lint"))
		assertThat(lintTasks.last(), equalTo(":lint"))
		result.assertSuccess(":module1:lint")
		result.assertSuccess(":module3:lint")
		result.assertSuccess(":lint")
		// se.bjurr.violations.lib.reports.Parser.findViolations swallows exceptions, so must check logs
		result.assertNoOutputLine(Regex("""Error when parsing.* as ANDROIDLINT"""))
		return result
	}

	@Test fun `fails the build on explicit invocation`() {
		`set up 3 modules with a lint failures`()

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.quality")
			}
		""".trimIndent()

		val result = gradle.runFailingBuild {
			basedOn("android-multi_module")
			run(script, "lint", ":lint")
		}

		result.assertFailed(":lint")
		result.assertHasOutputLine(Regex("""> Ran lint on subprojects: ${(1 + 1 + 1) * variantMultiplier} issues found\."""))
	}

	private fun `set up 3 modules with a lint failures`() {
		@Language("java")
		val lintViolation = """
			class LintFailure {
				void f() {
					android.util.Log.e("123456789012345678901234", "");
				}
			}
		""".trimIndent()
		val modules = arrayOf(
			"module1",
			"module2",
			"module3"
		)
		modules.forEach { module ->
			@Language("gradle")
			val subProject = """
				plugins {
					id("com.android.library")
				}
				android.namespace = "project.${module}"
				android.lint.checkOnly.add("LongLogTag")
			""".trimIndent()

			gradle.file(subProject, module, "build.gradle")
			gradle.settingsFile.appendText("""include(":${module}")${System.lineSeparator()}""")
			gradle.file(lintViolation, module, "src", "main", "java", "fail1.java")
		}
	}
}
