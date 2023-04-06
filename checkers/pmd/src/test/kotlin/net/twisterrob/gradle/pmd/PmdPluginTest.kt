package net.twisterrob.gradle.pmd

import net.twisterrob.gradle.BaseIntgTest
import net.twisterrob.gradle.pmd.test.PmdTestResources
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.failReason
import net.twisterrob.gradle.test.minus
import net.twisterrob.gradle.test.runBuild
import net.twisterrob.gradle.test.runFailingBuild
import net.twisterrob.gradle.test.tasksIn
import org.gradle.api.plugins.quality.Pmd
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.containsStringIgnoringCase
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasItems
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.startsWith
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

/**
 * @see PmdPlugin
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class PmdPluginTest : BaseIntgTest() {

	companion object {

		private val endl = System.lineSeparator()
	}

	override lateinit var gradle: GradleRunnerRule

	private val pmd = PmdTestResources()

	@Test fun `does not apply to empty project`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.pmd")
			}
		""".trimIndent()

		val result = gradle.runFailingBuild {
			run(script, "pmd")
		}

		assertThat(result.failReason, startsWith("Task 'pmd' not found"))
	}

	@Test fun `does not apply to a Java project`() {
		@Language("gradle")
		val script = """
			plugins {
				id("org.gradle.java")
				id("net.twisterrob.gradle.plugin.pmd")
			}
		""".trimIndent()

		val result = gradle.runFailingBuild {
			run(script, "pmd")
		}

		assertThat(result.failReason, startsWith("Task 'pmd' not found"))
	}

	@Test fun `applies without a hitch to an Android project`() {
		gradle.file(pmd.empty.config, "config", "pmd", "pmd.xml")
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.pmd")
			}
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
		gradle.basedOn("android-all_kinds")
		gradle.file(pmd.empty.config, "config", "pmd", "pmd.xml")
		// TODO add :dynamic-feature
		val modules = arrayOf(":app", ":library", ":library:nested", ":test")
		modules.forEach { modulePath ->
			@Language("gradle")
			val subProject = """
				plugins {
					id("net.twisterrob.gradle.plugin.pmd")
				}
			""".trimIndent()
			val subPath = modulePath.substringAfter(':').split(":").toTypedArray()
			gradle.file(subProject, GradleRunnerRule.TouchMode.MERGE_GRADLE, *subPath, "build.gradle")
		}
		// Add empty manifest, so PMD task picks it up.
		gradle.file("<manifest />", "library", "src", "main", "AndroidManifest.xml")
		gradle.file("<manifest />", "library", "nested", "src", "main", "AndroidManifest.xml")
		gradle.file("<manifest />", "test", "src", "main", "AndroidManifest.xml")

		val result = gradle.runBuild {
			run(null, "pmdEach")
		}

		val exceptions = arrayOf(
			// These tasks are not generated because their modules are special.
			":test:pmdRelease",
			*tasksIn(arrayOf(":base"), "pmdEach", "pmdRelease", "pmdDebug")
		)
		assertThat(
			result.taskPaths(TaskOutcome.SUCCESS),
			hasItems(*tasksIn(modules, "pmdRelease", "pmdDebug") - exceptions)
		)
		assertThat(
			result.taskPaths(TaskOutcome.SUCCESS),
			hasItems(*tasksIn(modules, "pmdEach") - exceptions)
		)
		val allTasks = result.tasks.map { it.path }
		val tasks = tasksIn(modules, "pmdEach", "pmdRelease", "pmdDebug") - exceptions
		assertThat(allTasks - tasks, not(hasItem(containsStringIgnoringCase("pmd"))))
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
		modules.forEach { modulePath ->
			gradle.settingsFile.appendText("include '${modulePath}'${endl}")

			@Language("gradle")
			val subProject = """
				plugins {
					id("com.android.library")
				}
				android.namespace = "project${modulePath.replace(":", ".")}"
			""".trimIndent()

			val subPath = modulePath.split(":").toTypedArray()
			gradle.file(subProject, *subPath, "build.gradle")
			// Add empty manifest, so PMD task picks it up.
			gradle.file("<manifest />", *subPath, "src", "main", "AndroidManifest.xml")
		}

		gradle.file(pmd.empty.config, "config", "pmd", "pmd.xml")

		@Language("gradle")
		val rootProject = """
			plugins {
				id("net.twisterrob.gradle.plugin.pmd") apply false
			}
			allprojects {
				apply plugin: "net.twisterrob.gradle.plugin.pmd"
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
		assertThat(allTasks - tasks, not(hasItem(containsStringIgnoringCase("pmd"))))
	}

	@Test fun `applies to individual subprojects`() {
		val modules = arrayOf(
			":module1",
			":module2",
			":module2:sub1",
			":module2:sub2",
			":module3:sub1",
			":module3:sub2"
		)
		val applyTo = arrayOf(":module2", ":module2:sub1", ":module3:sub2")
		modules.forEach { modulePath ->
			gradle.settingsFile.appendText("include '${modulePath}'${endl}")

			@Suppress("MandatoryBracesIfStatements") // Language annotation doesn't work on implicit block return.
			@Language("gradle")
			val subProject = if (modulePath in applyTo)
				"""
					plugins {
						id("net.twisterrob.gradle.plugin.pmd")
						id("com.android.library")
					}
					android.namespace = "project${modulePath.replace(":", ".")}"
				""".trimIndent()
			else
				"""
					plugins {
						id("com.android.library")
					}
					android.namespace = "project${modulePath.replace(":", ".")}"
				""".trimIndent()

			val subPath = modulePath.split(":").toTypedArray()
			gradle.file(subProject, *subPath, "build.gradle")
			// Add empty manifest, so PMD task picks it up.
			gradle.file("<manifest />", *subPath, "src", "main", "AndroidManifest.xml")
		}

		gradle.file(pmd.empty.config, "config", "pmd", "pmd.xml")

		val result = gradle.runBuild {
			basedOn("android-multi_module")
			run(null, "pmdEach")
		}

		val allTasks = result.tasks.map { it.path }
		val tasks = tasksIn(applyTo, "pmdEach", "pmdRelease", "pmdDebug")
		assertThat(allTasks - tasks, not(hasItem(containsStringIgnoringCase("pmd"))))

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
			plugins {
				id("net.twisterrob.gradle.plugin.pmd")
			}
			pmd {
				toolVersion = '5.6.1'
				incrementalAnalysis.set(false)
			}
			tasks.withType(${Pmd::class.java.name}).configureEach {
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

	@Test fun `applying by the old name is deprecated`() {
		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.pmd")
			}
		""".trimIndent()
		val result = gradle.run(script).buildAndFail()
		result.assertHasOutputLine(
			Regex(
				"""org\.gradle\.api\.GradleException: """ +
						"""Deprecated Gradle features were used in this build, making it incompatible with Gradle \d.0"""
			)
		)
		result.assertHasOutputLine(
			Regex(
				"""The net\.twisterrob\.pmd plugin has been deprecated\. """
						+ """This is scheduled to be removed in Gradle \d\.0\. """
						+ """Please use the net\.twisterrob\.gradle\.plugin\.pmd plugin instead."""
			)
		)
	}
}
