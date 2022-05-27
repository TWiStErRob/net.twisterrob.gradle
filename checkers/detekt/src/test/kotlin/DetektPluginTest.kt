package net.twisterrob.gradle.detekt

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.common.TaskConfigurator
import net.twisterrob.gradle.detekt.test.DetektTestResources
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertFailed
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoOutputLine
import net.twisterrob.gradle.test.assertNoSource
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.gradle.test.assertUpToDate
import net.twisterrob.gradle.test.failReason
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasItems
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.startsWith
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @see DetektPlugin
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class DetektPluginTest : BaseIntgTest() {

	companion object {

		private val endl = System.lineSeparator()
	}

	override lateinit var gradle: GradleRunnerRule
	private val detekt = DetektTestResources()

	@Test fun `does not apply to empty project`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.detekt'
		""".trimIndent()

		val result = gradle.run(script, "detekt").buildAndFail()

		assertThat(result.failReason, startsWith("Task 'detekt' not found"))
	}

	@Test fun `does not apply to a Java project`() {
		@Language("gradle")
		val script = """
			apply plugin: 'java'
			apply plugin: 'net.twisterrob.detekt'
		""".trimIndent()

		val result = gradle.run(script, "detekt").buildAndFail()

		assertThat(result.failReason, startsWith("Task 'detekt' not found"))
	}

	@Test fun `applies without a hitch to an Android project`() {
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.detekt'
		""".trimIndent()

		val result = gradle
				.basedOn("android-root_app")
				.run(script, "detektEach")
				.build()

		result.assertUpToDate(":detektEach")
		result.assertNoSource(":detektDebug")
		result.assertNoSource(":detektRelease")
	}

	@Test fun `applies to all types of subprojects`() {
		gradle.file(detekt.empty.config, "config", "detekt", "detekt.yml")
		@Language("gradle")
		val script = """
			allprojects {
				apply plugin: 'net.twisterrob.detekt'
			}
		""".trimIndent()
		// ":instant" is not supported yet
		val modules = arrayOf(":app", ":feature", ":base", ":library", ":library:nested", ":test")

		val result = gradle
				.basedOn("android-all_kinds")
				.run(script, "detektEach")
				.build()

		// these tasks are not generated because their modules are special
		val exceptions = arrayOf(":test:detektRelease")
		assertThat(result.taskPaths(TaskOutcome.NO_SOURCE),
				hasItems(*(tasksIn(modules, "detektRelease", "detektDebug") - exceptions)))
		assertThat(result.taskPaths(TaskOutcome.UP_TO_DATE),
				hasItems(*tasksIn(modules, "detektEach")))
		val allTasks = result.tasks.map { it.path }
		val tasks = tasksIn(modules, "detektEach", "detektRelease", "detektDebug") - exceptions
		assertThat(allTasks - tasks, not(hasItem(containsString("detekt"))))
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

		gradle.file(detekt.empty.config, "config", "detekt", "detekt.yml")

		@Language("gradle")
		val rootProject = """
			allprojects {
				apply plugin: 'net.twisterrob.detekt'
			}
		""".trimIndent()

		val result = gradle
				.basedOn("android-multi_module")
				.run(rootProject, "detektEach")
				.build()

		assertThat(result.taskPaths(TaskOutcome.NO_SOURCE),
				hasItems(*tasksIn(modules, "detektRelease", "detektDebug")))
		assertThat(result.taskPaths(TaskOutcome.UP_TO_DATE),
				hasItems(*tasksIn(modules, "detektEach")))
		val allTasks = result.tasks.map { it.path }
		val tasks = tasksIn(modules, "detektEach", "detektRelease", "detektDebug")
		assertThat(allTasks - tasks, not(hasItem(containsString("detekt"))))
	}

	@Test fun `applies to individual subprojects`() {
		@Language("gradle")
		val subProjectNotApplied = """
			apply plugin: 'com.android.library'
		""".trimIndent()
		@Language("gradle")
		val subProjectApplied = """
			apply plugin: 'net.twisterrob.detekt'
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
		modules.forEach { module ->
			gradle.settingsFile.appendText("include '${module}'${endl}")

			val subProject = if (module in applyTo) subProjectApplied else subProjectNotApplied
			@Language("xml")
			val manifest = """
				<manifest package="project${module.replace(":", ".")}" />
			""".trimIndent()

			val subPath = module.split(":").toTypedArray()
			gradle.file(subProject, *subPath, "build.gradle")
			gradle.file(manifest, *subPath, "src", "main", "AndroidManifest.xml")
		}

		gradle.file(detekt.empty.config, "config", "detekt", "detekt.yml")

		val result = gradle
				.basedOn("android-multi_module")
				.run(null, "detektEach")
				.build()

		val allTasks = result.tasks.map { it.path }
		val tasks = tasksIn(applyTo, "detektEach", "detektRelease", "detektDebug")
		assertThat(allTasks - tasks, not(hasItem(containsString("detekt"))))

		assertThat(result.taskPaths(TaskOutcome.NO_SOURCE),
				hasItems(*tasksIn(applyTo, "detektRelease", "detektDebug")))
		assertThat(result.taskPaths(TaskOutcome.UP_TO_DATE),
				hasItems(*tasksIn(applyTo, "detektEach")))
	}

	// TODO add more tests for modules
	@Test fun `basedir truncates folder names`() {
		gradle
				.basedOn("android-root_app")
				.basedOn("detekt-basedir")

		@Language("gradle")
		val applyCheckstyle = """
			apply plugin: 'net.twisterrob.detekt'
			tasks.withType(${Checkstyle::class.java.name}) {
				// output all violations to the console so that we can parse the results
				showViolations = true
			}
		""".trimIndent()

		val result = gradle.run(applyCheckstyle, ":detektDebug").buildAndFail()

		result.assertFailed(":detektDebug")
		assertThat(result.failReason, containsString("Checkstyle rule violations were found"))
		result.assertHasOutputLine(""".*\[ERROR] src.main.java.Checkstyle\.java:1: .*? \[Header]""".toRegex())
	}

	@Test fun `custom source sets folders are picked up`() {
		gradle.basedOn("android-root_app")
		gradle.file(detekt.simple.config, "config", "detekt", "detekt.yml")
		gradle.file(detekt.simple.content, "custom", "Detekt.kt")

		@Language("gradle")
		val build = """
			apply plugin: 'net.twisterrob.detekt'
			tasks.withType(${Checkstyle::class.java.name}) {
				// output all violations to the console so that we can parse the results
				showViolations = true
			}
			android.sourceSets.main.java.srcDir 'custom'
		""".trimIndent()

		val result = gradle.run(build, ":detektDebug").buildAndFail()

		result.assertFailed(":detektDebug")
		assertThat(result.failReason, containsString("Checkstyle rule violations were found"))
		result.assertHasOutputLine(detekt.simple.message)
	}

	@Test fun `exclusions are configurable per variant`() {
		gradle.basedOn("android-root_app")
		gradle.file(detekt.simple.config, "config", "detekt", "detekt.yml")
		gradle.file(detekt.simple.content, "src", "main", "java", "com", "example", "foo", "Detekt.kt")
		gradle.file(detekt.simple.content, "src", "main", "java", "com", "example", "bar", "Detekt.kt")
		gradle.file(detekt.simple.content, "src", "main", "java", "com", "example", "bar", "baz", "Detekt.kt")

		@Language("gradle")
		val build = """
			apply plugin: 'net.twisterrob.detekt'
			tasks.withType(${Checkstyle::class.java.name}) {
				// output all violations to the console so that we can parse the results
				showViolations = true
			}
			quality {
				detekt { // this : ${DetektExtension::class}
					taskConfigurator { // this : ${TaskConfigurator::class}
						excludeExcept '**/com/example', 'foo'
					}
				}
			}
		""".trimIndent()

		val result = gradle.run(build, ":detektDebug").buildAndFail()

		result.assertFailed(":detektDebug")
		assertThat(result.failReason, containsString("Checkstyle rule violations were found"))
		result.assertHasOutputLine(""".*com.example.foo.Checkstyle\.java:1: .*? \[Header]""".toRegex())
		result.assertNoOutputLine(""".*com.example.bar.Checkstyle\.java.*""".toRegex())
		result.assertNoOutputLine(""".*com.example.bar.baz.Checkstyle\.java.*""".toRegex())
	}

	// TODO test other properties
	@Test fun `config_loc allows to use local files`() {
		gradle
				.basedOn("android-root_app")
				.basedOn("detekt-config_loc")

		@Language("gradle")
		val applyCheckstyle = """
			apply plugin: 'net.twisterrob.detekt'
		""".trimIndent()

		val result = gradle.run(applyCheckstyle, ":detektDebug").build()

		result.assertSuccess(":detektDebug")
	}
}

private fun tasksIn(modules: Array<String>, vararg taskNames: String): Array<String> =
		modules
				.flatMap { module -> taskNames.map { taskName -> "${module}:${taskName}" } }
				.toTypedArray()

private inline operator fun <reified T> Array<T>.minus(others: Array<T>): Array<T> =
		(this.toList() - others).toTypedArray()
