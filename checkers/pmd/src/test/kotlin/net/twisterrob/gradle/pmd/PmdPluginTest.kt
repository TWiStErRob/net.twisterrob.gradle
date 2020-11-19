package net.twisterrob.gradle.pmd

import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.failReason
import net.twisterrob.gradle.test.runBuild
import net.twisterrob.gradle.test.runFailingBuild
import net.twisterrob.gradle.test.systemProperty
import org.gradle.api.plugins.quality.Pmd
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasItems
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.startsWith
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

/**
 * @see PmdPlugin
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class PmdPluginTest {

	companion object {

		private val endl = System.lineSeparator()
	}

	private lateinit var gradle: GradleRunnerRule

	@Test fun `does not apply to empty project`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.pmd'
		""".trimIndent()

		val result = gradle.runFailingBuild {
			run(script, "pmd")
		}

		assertThat(result.failReason, startsWith("Task 'pmd' not found"))
	}

	@Test fun `does not apply to a Java project`() {
		@Language("gradle")
		val script = """
			apply plugin: 'java'
			apply plugin: 'net.twisterrob.pmd'
		""".trimIndent()

		val result = gradle.runFailingBuild {
			run(script, "pmd")
		}

		assertThat(result.failReason, startsWith("Task 'pmd' not found"))
	}

	@Test fun `applies without a hitch to an Android project`() {
		gradle.file(gradle.templateFile("pmd-empty.xml").readText(), "config", "pmd", "pmd.xml")
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.pmd'
		""".trimIndent()

		val result = gradle.runBuild {
			basedOn("android-root_app")
			run(script, "pmdEach")
		}

		assertEquals(TaskOutcome.SUCCESS, result.task(":pmdEach")!!.outcome)
		assertEquals(TaskOutcome.SUCCESS, result.task(":pmdDebug")!!.outcome)
		assertEquals(TaskOutcome.SUCCESS, result.task(":pmdRelease")!!.outcome)
	}

	@Test fun `applies to all types of subprojects`() {
		gradle.file(gradle.templateFile("pmd-empty.xml").readText(), "config", "pmd", "pmd.xml")
		@Language("gradle")
		val script = """
			allprojects {
				apply plugin: 'net.twisterrob.pmd'
			}
		""".trimIndent()
		// ":instant" is not supported yet, and won't be since it's deprecated in 3.6.x.
		val modules = arrayOf(":app", ":library", ":library:nested", ":test") +
				if (System.getProperty("net.twisterrob.test.android.pluginVersion") < "3.4.0")
					arrayOf(":feature", ":base")
				else
					emptyArray() // TODO arrayOf(":dynamic-feature")

		val result = gradle.runBuild {
			basedOn("android-all_kinds")
			run(script, "pmdEach")
		}

		// these tasks are not generated because their modules are special
		val exceptions = arrayOf(":test:pmdRelease")
		assertThat(
			result.taskPaths(TaskOutcome.SUCCESS),
			hasItems(*(tasksIn(modules, "pmdRelease", "pmdDebug") - exceptions))
		)
		assertThat(
			result.taskPaths(TaskOutcome.SUCCESS),
			hasItems(*tasksIn(modules, "pmdEach"))
		)
		val allTasks = result.tasks.map { it.path }
		val tasks = tasksIn(modules, "pmdEach", "pmdRelease", "pmdDebug") - exceptions
		assertThat(allTasks - tasks, not(hasItem(containsString("pmd"))))
	}

	@Test fun `applies to subprojects from root`() {
		val modules = arrayOf(
			":module1",
			":module2",
			":module2:sub1",
			":module2:sub2",
			":module3:sub1",
			":module3:sub2"
		)
		modules.forEach {
			gradle.settingsFile.appendText("include '${it}'${endl}")

			@Language("gradle")
			val subProject = """
				apply plugin: 'com.android.library'
			""".trimIndent()

			@Language("xml")
			val manifest = """
				<manifest package="project${it.replace(":", ".")}" />
			""".trimIndent()

			val subPath = it.split(":").toTypedArray()
			gradle.file(subProject, *subPath, "build.gradle")
			gradle.file(manifest, *subPath, "src", "main", "AndroidManifest.xml")
		}

		gradle.file(gradle.templateFile("pmd-empty.xml").readText(), "config", "pmd", "pmd.xml")

		@Language("gradle")
		val rootProject = """
			allprojects {
				apply plugin: 'net.twisterrob.pmd'
			}
		""".trimIndent()

		val result = gradle.runBuild {
			basedOn("android-multi_module")
			run(rootProject, "pmdEach")
		}

		assertThat(
			result.taskPaths(TaskOutcome.SUCCESS),
			hasItems(*tasksIn(modules, "pmdRelease", "pmdDebug"))
		)
		assertThat(
			result.taskPaths(TaskOutcome.SUCCESS),
			hasItems(*tasksIn(modules, "pmdEach"))
		)
		val allTasks = result.tasks.map { it.path }
		val tasks = tasksIn(modules, "pmdEach", "pmdRelease", "pmdDebug")
		assertThat(allTasks - tasks, not(hasItem(containsString("pmd"))))
	}

	@Test fun `applies to individual subprojects`() {
		@Language("gradle")
		val subProjectNotApplied = """
			apply plugin: 'com.android.library'
		""".trimIndent()
		@Language("gradle")
		val subProjectApplied = """
			apply plugin: 'net.twisterrob.pmd'
			apply plugin: 'com.android.library'
		""".trimIndent()

		val modules = arrayOf(
			":module1",
			":module2",
			":module2:sub1",
			":module2:sub2",
			":module3:sub1",
			":module3:sub2"
		)
		val applyTo = arrayOf(":module2", ":module2:sub1", ":module3:sub2")
		modules.forEach {
			gradle.settingsFile.appendText("include '${it}'${endl}")

			val subProject = if (it in applyTo) subProjectApplied else subProjectNotApplied
			@Language("xml")
			val manifest = """
				<manifest package="project${it.replace(":", ".")}" />
			""".trimIndent()

			val subPath = it.split(":").toTypedArray()
			gradle.file(subProject, *subPath, "build.gradle")
			gradle.file(manifest, *subPath, "src", "main", "AndroidManifest.xml")
		}

		gradle.file(gradle.templateFile("pmd-empty.xml").readText(), "config", "pmd", "pmd.xml")

		val result = gradle.runBuild {
			basedOn("android-multi_module")
			run(null, "pmdEach")
		}

		val allTasks = result.tasks.map { it.path }
		val tasks = tasksIn(applyTo, "pmdEach", "pmdRelease", "pmdDebug")
		assertThat(allTasks - tasks, not(hasItem(containsString("pmd"))))

		assertThat(
			result.taskPaths(TaskOutcome.SUCCESS),
			hasItems(*tasksIn(applyTo, "pmdRelease", "pmdDebug"))
		)
		assertThat(
			result.taskPaths(TaskOutcome.SUCCESS),
			hasItems(*tasksIn(applyTo, "pmdEach"))
		)
	}

	@Test fun `allows ruleset inclusion from all sources`() {
		gradle
			.basedOn("android-root_app")
			.basedOn("pmd-multi_file_config")

		@Language("gradle")
		val applyPmd = """
			import org.gradle.util.GradleVersion
			apply plugin: 'net.twisterrob.pmd'
			pmd {
				toolVersion = '5.6.1'
				if (GradleVersion.version("6.0.0") <= GradleVersion.current()) {
					incrementalAnalysis.set(false)
				}
			}
			tasks.withType(${Pmd::class.java.name}) {
				// output all violations to the console so that we can parse the results
				consoleOutput = true
			}
		""".trimIndent()

		val result = gradle.runFailingBuild {
			run(applyPmd, ":pmdDebug")
		}

		assertEquals(TaskOutcome.FAILED, result.task(":pmdDebug")!!.outcome)
		result.assertHasOutputLine(
			"Inline rule violation",
			Regex(""".*src.main.java.Pmd\.java:2:\s+Inline custom message""")
		)
		result.assertHasOutputLine(
			"Inline rule reference violation",
			Regex(""".*src.main.java.Pmd\.java:3:\s+Avoid using short method names""")
		)
		result.assertHasOutputLine(
			"Included ruleset from the same folder violation",
			Regex(""".*src.main.java.Pmd\.java:4:\s+Avoid variables with short names like i""")
		)
		result.assertHasOutputLine(
			"Included ruleset from a sub-folder violation",
			Regex(""".*src.main.java.Pmd\.java:2:\s+All classes and interfaces must belong to a named package""")
		)
		assertThat(
			"Validate count to allow no more violations",
			result.failReason, containsString("4 PMD rule violations were found.")
		)
	}
}

private fun tasksIn(modules: Array<String>, vararg taskNames: String): Array<String> =
	modules
		.flatMap { module -> taskNames.map { taskName -> "${module}:${taskName}" } }
		.toTypedArray()

private inline operator fun <reified T> Array<T>.minus(others: Array<T>) =
	(this.toList() - others).toTypedArray()
