package net.twisterrob.gradle.pmd

import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.failReason
import org.gradle.api.plugins.quality.Pmd
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasItems
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.startsWith
import org.intellij.lang.annotations.Language
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test

class PmdPluginTest {

	companion object {

		private val endl = System.lineSeparator()
	}

	@Rule @JvmField val gradle = GradleRunnerRule()

	@Test fun `does not apply to empty project`() {
		`given`@
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.pmd'
		""".trimIndent()

		val result: BuildResult
		`when`@
		result = gradle.run(script, "pmd").buildAndFail()

		`then`@
		assertThat(result.failReason, startsWith("Task 'pmd' not found"))
	}

	@Test fun `does not apply to a Java project`() {
		`given`@
		@Language("gradle")
		val script = """
			apply plugin: 'java'
			apply plugin: 'net.twisterrob.pmd'
		""".trimIndent()

		val result: BuildResult
		`when`@
		result = gradle.run(script, "pmd").buildAndFail()

		`then`@
		assertThat(result.failReason, startsWith("Task 'pmd' not found"))
	}

	@Test fun `applies without a hitch to an Android project`() {
		`given`@
		gradle.file(gradle.templateFile("pmd-empty.xml").readText(), "config", "pmd", "pmd.xml")
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.pmd'
		""".trimIndent()

		val result: BuildResult
		`when`@
		result = gradle
				.basedOn("android-root_app")
				.run(script, "pmdEach")
				.build()

		`then`@
		assertEquals(TaskOutcome.SUCCESS, result.task(":pmdEach")!!.outcome)
		assertEquals(TaskOutcome.SUCCESS, result.task(":pmdDebug")!!.outcome)
		assertEquals(TaskOutcome.SUCCESS, result.task(":pmdRelease")!!.outcome)
	}

	@Test fun `applies to all types of subprojects`() {
		`given`@
		gradle.file(gradle.templateFile("pmd-empty.xml").readText(), "config", "pmd", "pmd.xml")
		@Language("gradle")
		val script = """
			allprojects {
				apply plugin: 'net.twisterrob.pmd'
			}
		""".trimIndent()
		// ":instant" is not supported yet 
		val modules = arrayOf(":app", ":feature", ":base", ":library", ":library:nested", ":test")

		val result: BuildResult
		`when`@
		result = gradle
				.basedOn("android-all_kinds")
				.run(script, "pmdEach")
				.build()

		// these tasks are not generated because their modules are special
		val exceptions = arrayOf(":test:pmdRelease")
		`then`@
		assertThat(result.taskPaths(TaskOutcome.SUCCESS),
				hasItems(*(tasksIn(modules, "pmdRelease", "pmdDebug") - exceptions)))
		assertThat(result.taskPaths(TaskOutcome.SUCCESS),
				hasItems(*tasksIn(modules, "pmdEach")))
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
		`given`@
		modules.forEach {
			gradle.settingsFile().appendText("include '${it}'${endl}")

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

		val result: BuildResult
		`when`@
		result = gradle
				.basedOn("android-multi_module")
				.run(rootProject, "pmdEach")
				.build()

		`then`@
		assertThat(result.taskPaths(TaskOutcome.SUCCESS),
				hasItems(*tasksIn(modules, "pmdRelease", "pmdDebug")))
		assertThat(result.taskPaths(TaskOutcome.SUCCESS),
				hasItems(*tasksIn(modules, "pmdEach")))
		val allTasks = result.tasks.map { it.path }
		val tasks = tasksIn(modules, "pmdEach", "pmdRelease", "pmdDebug")
		assertThat(allTasks - tasks, not(hasItem(containsString("pmd"))))
	}

	@Test fun `applies to individual subprojects`() {
		`given`@
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
			gradle.settingsFile().appendText("include '${it}'${endl}")

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

		val result: BuildResult
		`when`@
		result = gradle
				.basedOn("android-multi_module")
				.run(null, "pmdEach")
				.build()

		val allTasks = result.tasks.map { it.path }
		val tasks = tasksIn(applyTo, "pmdEach", "pmdRelease", "pmdDebug")
		`then`@
		assertThat(allTasks - tasks, not(hasItem(containsString("pmd"))))

		assertThat(result.taskPaths(TaskOutcome.SUCCESS),
				hasItems(*tasksIn(applyTo, "pmdRelease", "pmdDebug")))
		assertThat(result.taskPaths(TaskOutcome.SUCCESS),
				hasItems(*tasksIn(applyTo, "pmdEach")))
	}

	@Test fun `allows ruleset inclusion from all sources`() {
		`given`@
		gradle
				.basedOn("android-root_app")
				.basedOn("pmd-multi_file_config")

		@Language("gradle")
		val applyPmd = """
			apply plugin: 'net.twisterrob.pmd'
			tasks.withType(${Pmd::class.java.name}) {
				// output all violations to the console so that we can parse the results
				consoleOutput = true
			}
		""".trimIndent()

		val result: BuildResult
		`when`@
		result = gradle.run(applyPmd, ":pmdDebug").buildAndFail()

		`then`@
		assertEquals(TaskOutcome.FAILED, result.task(":pmdDebug")!!.outcome)
		result.assertHasOutputLine("Inline rule violation",
				""".*src.main.java.Pmd\.java:2:\s+Inline custom message""".toRegex())
		result.assertHasOutputLine("Inline rule reference violation",
				""".*src.main.java.Pmd\.java:3:\s+Avoid using short method names""".toRegex())
		result.assertHasOutputLine("Included ruleset from the same folder violation",
				""".*src.main.java.Pmd\.java:4:\s+Avoid variables with short names like i""".toRegex())
		result.assertHasOutputLine("Included ruleset from a sub-folder violation",
				""".*src.main.java.Pmd\.java:2:\s+All classes and interfaces must belong to a named package""".toRegex())
		assertThat("Validate count to allow no more violations",
				result.failReason, containsString("4 PMD rule violations were found."))
	}
}

private fun tasksIn(modules: Array<String>, vararg taskNames: String): Array<String> =
		modules
				.flatMap { module -> taskNames.map { taskName -> "${module}:${taskName}" } }
				.toTypedArray()

private inline operator fun <reified T> Array<T>.minus(others: Array<T>) =
		(this.toList() - others).toTypedArray()
