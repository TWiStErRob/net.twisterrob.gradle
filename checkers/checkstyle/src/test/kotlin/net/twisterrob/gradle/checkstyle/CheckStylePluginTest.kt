package net.twisterrob.gradle.checkstyle

import net.twisterrob.gradle.common.TaskConfigurator
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertNoOutputLine
import net.twisterrob.gradle.test.failReason
import org.gradle.api.plugins.quality.Checkstyle
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

class CheckStylePluginTest {

	companion object {

		private val endl = System.lineSeparator()
	}

	@Rule @JvmField val gradle = GradleRunnerRule(false)

	@Test fun `does not apply to empty project`() {
		`given`@
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.checkstyle'
		""".trimIndent()

		val result: BuildResult
		`when`@
		result = gradle.run(script, "checkstyle").buildAndFail()

		`then`@
		assertThat(result.failReason, startsWith("Task 'checkstyle' not found"))
	}

	@Test fun `does not apply to a Java project`() {
		`given`@
		@Language("gradle")
		val script = """
			apply plugin: 'java'
			apply plugin: 'net.twisterrob.checkstyle'
		""".trimIndent()

		val result: BuildResult
		`when`@
		result = gradle
				.run(script, "checkstyle").buildAndFail()

		`then`@
		assertThat(result.failReason, startsWith("Task 'checkstyle' not found"))
	}

	@Test fun `applies without a hitch to an Android project`() {
		`given`@
		@Language("gradle")
		val script = """
			apply plugin: 'net.twisterrob.checkstyle'
		""".trimIndent()

		val result: BuildResult
		`when`@
		result = gradle
				.basedOn("android-root_app")
				.run(script, "checkstyleEach")
				.build()

		`then`@
		assertEquals(TaskOutcome.UP_TO_DATE, result.task(":checkstyleEach")!!.outcome)
		assertEquals(TaskOutcome.NO_SOURCE, result.task(":checkstyleDebug")!!.outcome)
		assertEquals(TaskOutcome.NO_SOURCE, result.task(":checkstyleRelease")!!.outcome)
	}

	@Test fun `applies to all types of subprojects`() {
		`given`@
		gradle.file(gradle.templateFile("checkstyle-empty.xml").readText(), "config", "checkstyle", "checkstyle.xml")
		@Language("gradle")
		val script = """
			allprojects {
				apply plugin: 'net.twisterrob.checkstyle'
			}
		""".trimIndent()
		// ":instant" is not supported yet 
		val modules = arrayOf(":app", ":feature", ":base", ":library", ":library:nested", ":test")

		val result: BuildResult
		`when`@
		result = gradle
				.basedOn("android-all_kinds")
				.run(script, "checkstyleEach")
				.build()

		// these tasks are not generated because their modules are special
		val exceptions = arrayOf(":test:checkstyleRelease")
		`then`@
		assertThat(result.taskPaths(TaskOutcome.NO_SOURCE),
				hasItems(*(tasksIn(modules, "checkstyleRelease", "checkstyleDebug") - exceptions)))
		assertThat(result.taskPaths(TaskOutcome.UP_TO_DATE),
				hasItems(*tasksIn(modules, "checkstyleEach")))
		val allTasks = result.tasks.map { it.path }
		val tasks = tasksIn(modules, "checkstyleEach", "checkstyleRelease", "checkstyleDebug") - exceptions
		assertThat(allTasks - tasks, not(hasItem(containsString("checkstyle"))))
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

		gradle.file(gradle.templateFile("checkstyle-empty.xml").readText(), "config", "checkstyle", "checkstyle.xml")

		@Language("gradle")
		val rootProject = """
			allprojects {
				apply plugin: 'net.twisterrob.checkstyle'
			}
		""".trimIndent()

		val result: BuildResult
		`when`@
		result = gradle
				.basedOn("android-multi_module")
				.run(rootProject, "checkstyleEach")
				.build()

		`then`@
		assertThat(result.taskPaths(TaskOutcome.NO_SOURCE),
				hasItems(*tasksIn(modules, "checkstyleRelease", "checkstyleDebug")))
		assertThat(result.taskPaths(TaskOutcome.UP_TO_DATE),
				hasItems(*tasksIn(modules, "checkstyleEach")))
		val allTasks = result.tasks.map { it.path }
		val tasks = tasksIn(modules, "checkstyleEach", "checkstyleRelease", "checkstyleDebug")
		assertThat(allTasks - tasks, not(hasItem(containsString("checkstyle"))))
	}

	@Test fun `applies to individual subprojects`() {
		`given`@
		@Language("gradle")
		val subProjectNotApplied = """
			apply plugin: 'com.android.library'
		""".trimIndent()
		@Language("gradle")
		val subProjectApplied = """
			apply plugin: 'net.twisterrob.checkstyle'
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
			gradle.settingsFile().appendText("include '${module}'${endl}")

			val subProject = if (module in applyTo) subProjectApplied else subProjectNotApplied
			@Language("xml")
			val manifest = """
				<manifest package="project${module.replace(":", ".")}" />
			""".trimIndent()

			val subPath = module.split(":").toTypedArray()
			gradle.file(subProject, *subPath, "build.gradle")
			gradle.file(manifest, *subPath, "src", "main", "AndroidManifest.xml")
		}

		gradle.file(gradle.templateFile("checkstyle-empty.xml").readText(), "config", "checkstyle", "checkstyle.xml")

		val result: BuildResult
		`when`@
		result = gradle
				.basedOn("android-multi_module")
				.run(null, "checkstyleEach")
				.build()

		val allTasks = result.tasks.map { it.path }
		val tasks = tasksIn(applyTo, "checkstyleEach", "checkstyleRelease", "checkstyleDebug")
		`then`@
		assertThat(allTasks - tasks, not(hasItem(containsString("checkstyle"))))

		assertThat(result.taskPaths(TaskOutcome.NO_SOURCE),
				hasItems(*tasksIn(applyTo, "checkstyleRelease", "checkstyleDebug")))
		assertThat(result.taskPaths(TaskOutcome.UP_TO_DATE),
				hasItems(*tasksIn(applyTo, "checkstyleEach")))
	}

	// TODO add more tests for modules
	@Test fun `basedir truncates folder names`() {
		`given`@
		gradle
				.basedOn("android-root_app")
				.basedOn("checkstyle-basedir")

		@Language("gradle")
		val applyCheckstyle = """
			apply plugin: 'net.twisterrob.checkstyle'
			tasks.withType(${Checkstyle::class.java.name}) {
				// output all violations to the console so that we can parse the results
				showViolations = true
			}
		""".trimIndent()

		val result: BuildResult
		`when`@
		result = gradle.run(applyCheckstyle, ":checkstyleDebug").buildAndFail()

		`then`@
		assertEquals(TaskOutcome.FAILED, result.task(":checkstyleDebug")!!.outcome)
		assertThat(result.failReason, containsString("Checkstyle rule violations were found"))
		result.assertHasOutputLine(""".*\[ERROR] src.main.java.Checkstyle\.java:1: .*? \[Header]""".toRegex())
	}

	@Test fun `custom source sets folders are picked up`() {
		`given`@
		gradle.basedOn("android-root_app")
		gradle.file(gradle.templateFile("checkstyle-simple_failure.xml").readText(), "config", "checkstyle", "checkstyle.xml")
		gradle.file(gradle.templateFile("checkstyle-simple_failure.java").readText(), "custom", "Checkstyle.java")

		@Language("gradle")
		val build = """
			apply plugin: 'net.twisterrob.checkstyle'
			tasks.withType(${Checkstyle::class.java.name}) {
				// output all violations to the console so that we can parse the results
				showViolations = true
			}
			android.sourceSets.main.java.srcDir 'custom'
		""".trimIndent()

		val result: BuildResult
		`when`@
		result = gradle.run(build, ":checkstyleDebug").buildAndFail()

		`then`@
		assertEquals(TaskOutcome.FAILED, result.task(":checkstyleDebug")!!.outcome)
		assertThat(result.failReason, containsString("Checkstyle rule violations were found"))
		result.assertHasOutputLine(""".*custom.Checkstyle\.java:1: .*? \[Header]""".toRegex())
	}

	@Test fun `exclusions are configurable per variant`() {
		`given`@
		gradle.basedOn("android-root_app")
		gradle.file(gradle.templateFile("checkstyle-simple_failure.xml").readText(), "config", "checkstyle", "checkstyle.xml")
		gradle.file(gradle.templateFile("checkstyle-simple_failure.java").readText(),
				"src", "main", "java", "com", "example", "foo", "Checkstyle.java")
		gradle.file(gradle.templateFile("checkstyle-simple_failure.java").readText(),
				"src", "main", "java", "com", "example", "bar", "Checkstyle.java")
		gradle.file(gradle.templateFile("checkstyle-simple_failure.java").readText(),
				"src", "main", "java", "com", "example", "bar", "baz", "Checkstyle.java")

		@Language("gradle")
		val build = """
			apply plugin: 'net.twisterrob.checkstyle'
			tasks.withType(${Checkstyle::class.java.name}) {
				// output all violations to the console so that we can parse the results
				showViolations = true
			}
			quality {
				checkstyle { // this : ${CheckStyleExtension::class}
					taskConfigurator { // this : ${TaskConfigurator::class}
						excludeExcept '**/com/example', 'foo'
					}
				}
			}
		""".trimIndent()

		val result: BuildResult
		`when`@
		result = gradle.run(build, ":checkstyleDebug").buildAndFail()

		`then`@
		assertEquals(TaskOutcome.FAILED, result.task(":checkstyleDebug")!!.outcome)
		assertThat(result.failReason, containsString("Checkstyle rule violations were found"))
		result.assertHasOutputLine(""".*com.example.foo.Checkstyle\.java:1: .*? \[Header]""".toRegex())
		result.assertNoOutputLine(""".*com.example.bar.Checkstyle\.java.*""".toRegex())
		result.assertNoOutputLine(""".*com.example.bar.baz.Checkstyle\.java.*""".toRegex())
	}

	// TODO test other properties
	@Test fun `config_loc allows to use local files`() {
		`given`@
		gradle
				.basedOn("android-root_app")
				.basedOn("checkstyle-config_loc")

		@Language("gradle")
		val applyCheckstyle = """
			apply plugin: 'net.twisterrob.checkstyle'
		""".trimIndent()

		val result: BuildResult
		`when`@
		result = gradle.run(applyCheckstyle, ":checkstyleDebug").build()

		`then`@
		assertEquals(TaskOutcome.SUCCESS, result.task(":checkstyleDebug")!!.outcome)
	}
}

private fun tasksIn(modules: Array<String>, vararg taskNames: String): Array<String> =
		modules
				.flatMap { module -> taskNames.map { taskName -> "${module}:${taskName}" } }
				.toTypedArray()

private inline operator fun <reified T> Array<T>.minus(others: Array<T>) =
		(this.toList() - others).toTypedArray()
