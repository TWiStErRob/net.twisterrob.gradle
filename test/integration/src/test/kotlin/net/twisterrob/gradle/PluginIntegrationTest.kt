package net.twisterrob.gradle

import net.twisterrob.gradle.common.KotlinVersions
import net.twisterrob.gradle.test.ContentMergeMode
import net.twisterrob.gradle.test.GradleBuildTestResources
import net.twisterrob.gradle.test.GradleBuildTestResources.basedOn
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertSuccess
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 * See all `gradlePlugin { plugins { create() { id =` blocks in this project's build.gradle.kts files.
 */
@ExtendWith(GradleRunnerRuleExtension::class)
class PluginIntegrationTest : BaseIntgTest() {

	override lateinit var gradle: GradleRunnerRule

	/**
	 * This is a meta-test, it's verifying the code in settings.gradle.kts that it's capable of flagging bad behavior.
	 */
	@Test
	fun `eagerly created task is detected`() {
		gradle.file(settings, ContentMergeMode.MERGE_GRADLE, "settings.gradle.kts")

		@Language("gradle")
		val script = """
			//noinspection ConfigurationAvoidance intentionally eager.
			task eagerlyCreated { }
		""".trimIndent()

		verifySingleTaskCreated(script, ":eagerlyCreated")
	}

	/**
	 * This is a meta-test, it's verifying the code in settings.gradle.kts that it's capable of flagging bad behavior.
	 */
	@Test
	fun `created task is detected`() {
		gradle.file(settings, ContentMergeMode.MERGE_GRADLE, "settings.gradle.kts")

		@Language("gradle")
		val script = """
			//noinspection ConfigurationAvoidance intentionally eager.
			tasks.create("eagerlyCreated") { }
		""".trimIndent()

		verifySingleTaskCreated(script, ":eagerlyCreated")
	}

	/**
	 * This is a meta-test, it's verifying the code in settings.gradle.kts that it's capable of flagging bad behavior.
	 */
	@Test
	fun `registered task is not detected`() {
		gradle.file(settings, ContentMergeMode.MERGE_GRADLE, "settings.gradle.kts")

		@Language("gradle")
		val script = """
			tasks.register("lazilyRegistered") { }
		""".trimIndent()

		verifyTasksNotCreated(script)
	}

	/**
	 * This is a meta-test, it's verifying the code in settings.gradle.kts that it's capable of flagging bad behavior.
	 */
	@Test
	fun `registered task that is materialized is detected`() {
		gradle.file(settings, ContentMergeMode.MERGE_GRADLE, "settings.gradle.kts")

		@Language("gradle")
		val script = """
			def task = tasks.register("lazilyRegistered") { }
			task.get()
		""".trimIndent()

		verifySingleTaskCreated(script, ":lazilyRegistered")
	}

	@ParameterizedTest(name = "{0} - {displayName}")
	@ValueSource(
		strings = [
			"net.twisterrob.gradle.plugin.root", // :plugin:base
			"net.twisterrob.gradle.plugin.java", // :plugin:languages
			"net.twisterrob.gradle.plugin.java-library", // :plugin:languages
			// Kotlin: "net.twisterrob.gradle.plugin.kotlin", // :plugin:languages
			"net.twisterrob.gradle.plugin.vcs", // :plugin:versioning
			// Android: "net.twisterrob.gradle.plugin.android-app", // :plugin
			// Android: "net.twisterrob.gradle.plugin.android-library", // :plugin
			// Android: "net.twisterrob.gradle.plugin.android-test", // :plugin
			"net.twisterrob.gradle.plugin.quality", // :quality
			"net.twisterrob.gradle.plugin.pmd", // :pmd
			"net.twisterrob.gradle.plugin.checkstyle", // :checkstyle
			"net.twisterrob.gradle.plugin.gradle.test", // :test
		]
	)
	fun `empty project doesn't create tasks when using plugin`(pluginId: String) {
		gradle.file(settings, ContentMergeMode.MERGE_GRADLE, "settings.gradle.kts")

		@Language("gradle")
		val script = """
			plugins {
				id("${pluginId}")
			}
		""".trimIndent()

		verifyTasksNotCreated(script)
	}

	@ParameterizedTest(name = "{0} - {displayName}")
	@ValueSource(
		strings = [
			"net.twisterrob.gradle.plugin.root", // :plugin:base
			"net.twisterrob.gradle.plugin.java", // :plugin:languages
			"net.twisterrob.gradle.plugin.java-library", // :plugin:languages
			"net.twisterrob.gradle.plugin.kotlin", // :plugin:languages
			"net.twisterrob.gradle.plugin.vcs", // :plugin:versioning
			// Android: "net.twisterrob.gradle.plugin.android-app", // :plugin
			// Android: "net.twisterrob.gradle.plugin.android-library", // :plugin
			// Android: "net.twisterrob.gradle.plugin.android-test", // :plugin
			"net.twisterrob.gradle.plugin.quality", // :quality
			"net.twisterrob.gradle.plugin.pmd", // :pmd
			"net.twisterrob.gradle.plugin.checkstyle", // :checkstyle
			"net.twisterrob.gradle.plugin.gradle.test", // :test
		]
	)
	fun `kotlin project doesn't create tasks when using plugin`(pluginId: String) {
		gradle.basedOn(GradleBuildTestResources.kotlin)
		gradle.file(settings, ContentMergeMode.MERGE_GRADLE, "settings.gradle.kts")

		@Language("gradle")
		val script = """
			plugins {
				id("${pluginId}")
			}
		""".trimIndent()

		verifyTasksNotCreated(script)
	}

	@ParameterizedTest(name = "{0} - {displayName}")
	@ValueSource(
		strings = [
			"net.twisterrob.gradle.plugin.root", // :plugin:base
			"net.twisterrob.gradle.plugin.java", // :plugin:languages
			"net.twisterrob.gradle.plugin.java-library", // :plugin:languages
			// Kotlin: "net.twisterrob.gradle.plugin.kotlin", // :plugin:languages
			"net.twisterrob.gradle.plugin.vcs", // :plugin:versioning
			"net.twisterrob.gradle.plugin.quality", // :quality
			"net.twisterrob.gradle.plugin.pmd", // :pmd
			"net.twisterrob.gradle.plugin.checkstyle", // :checkstyle
			// Android: "net.twisterrob.gradle.plugin.gradle.test", // :test
		]
	)
	fun `android app project doesn't create tasks when using plugin`(pluginId: String) {
		gradle.basedOn(GradleBuildTestResources.android)
		// Default build.gradle has the app plugin applied.
		gradle.buildFile.writeText(gradle.buildFile.readText().replace("id(\"com.android.application\")", ""))
		gradle.file(settings, ContentMergeMode.MERGE_GRADLE, "settings.gradle.kts")

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-app")
				id("${pluginId}")
			}
		""".trimIndent()

		verifyTasksNotCreated(script)
	}

	@ParameterizedTest(name = "{0} - {displayName}")
	@ValueSource(
		strings = [
			"net.twisterrob.gradle.plugin.root", // :plugin:base
			"net.twisterrob.gradle.plugin.java", // :plugin:languages
			"net.twisterrob.gradle.plugin.java-library", // :plugin:languages
			// Kotlin: "net.twisterrob.gradle.plugin.kotlin", // :plugin:languages
			"net.twisterrob.gradle.plugin.vcs", // :plugin:versioning
			"net.twisterrob.gradle.plugin.quality", // :quality
			"net.twisterrob.gradle.plugin.pmd", // :pmd
			"net.twisterrob.gradle.plugin.checkstyle", // :checkstyle
			// Android: "net.twisterrob.gradle.plugin.gradle.test", // :test
		]
	)
	fun `android library project doesn't create tasks when using plugin`(pluginId: String) {
		gradle.basedOn(GradleBuildTestResources.android)
		// Default build.gradle has the app plugin applied.
		gradle.buildFile.writeText(gradle.buildFile.readText().replace("id(\"com.android.application\")", ""))
		gradle.file(settings, ContentMergeMode.MERGE_GRADLE, "settings.gradle.kts")

		@Language("gradle")
		val script = """
			plugins {
				id("net.twisterrob.gradle.plugin.android-library")
				id("${pluginId}")
			}
		""".trimIndent()

		verifyTasksNotCreated(script)
	}

	@ParameterizedTest(name = "{0} - {displayName}")
	@ValueSource(
		strings = [
			"net.twisterrob.gradle.plugin.root", // :plugin:base
			"net.twisterrob.gradle.plugin.java", // :plugin:languages
			"net.twisterrob.gradle.plugin.java-library", // :plugin:languages
			"net.twisterrob.gradle.plugin.kotlin", // :plugin:languages
			"net.twisterrob.gradle.plugin.vcs", // :plugin:versioning
			// Android: "net.twisterrob.gradle.plugin.android-app", // :plugin
			// Android: "net.twisterrob.gradle.plugin.android-library", // :plugin
			// Special: "net.twisterrob.gradle.plugin.android-test", // :plugin
			"net.twisterrob.gradle.plugin.quality", // :quality
			"net.twisterrob.gradle.plugin.pmd", // :pmd
			"net.twisterrob.gradle.plugin.checkstyle", // :checkstyle
			// Android: "net.twisterrob.gradle.plugin.gradle.test", // :test
		]
	)
	fun `android kotlin app project doesn't create tasks when using plugin`(pluginId: String) {
		gradle.basedOn(GradleBuildTestResources.android)
		// Default build.gradle has the app plugin applied.
		gradle.buildFile.writeText(gradle.buildFile.readText().replace("id(\"com.android.application\")", ""))
		gradle.basedOn(GradleBuildTestResources.kotlin)
		gradle.file(settings, ContentMergeMode.MERGE_GRADLE, "settings.gradle.kts")

		val kotlinPluginApply = conditionalApplyKotlin("net.twisterrob.gradle.plugin.android-app")
		@Language("gradle")
		val script = kotlinPluginApply + """
			plugins {
				id("${pluginId}")
			}
		""".trimIndent()

		verifyTasksNotCreated(script)
	}

	@ParameterizedTest(name = "{0} - {displayName}")
	@ValueSource(
		strings = [
			"net.twisterrob.gradle.plugin.root", // :plugin:base
			"net.twisterrob.gradle.plugin.java", // :plugin:languages
			"net.twisterrob.gradle.plugin.java-library", // :plugin:languages
			"net.twisterrob.gradle.plugin.kotlin", // :plugin:languages
			"net.twisterrob.gradle.plugin.vcs", // :plugin:versioning
			// Android: "net.twisterrob.gradle.plugin.android-app", // :plugin
			// Android: "net.twisterrob.gradle.plugin.android-library", // :plugin
			// Special: "net.twisterrob.gradle.plugin.android-test", // :plugin
			"net.twisterrob.gradle.plugin.quality", // :quality
			"net.twisterrob.gradle.plugin.pmd", // :pmd
			"net.twisterrob.gradle.plugin.checkstyle", // :checkstyle
			// Android: "net.twisterrob.gradle.plugin.gradle.test", // :test
		]
	)
	fun `android kotlin library project doesn't create tasks when using plugin`(pluginId: String) {
		gradle.basedOn(GradleBuildTestResources.android)
		// Default build.gradle has the app plugin applied.
		gradle.buildFile.writeText(gradle.buildFile.readText().replace("id(\"com.android.application\")", ""))
		gradle.basedOn(GradleBuildTestResources.kotlin)
		gradle.file(settings, ContentMergeMode.MERGE_GRADLE, "settings.gradle.kts")

		val kotlinPluginApply = conditionalApplyKotlin("net.twisterrob.gradle.plugin.android-library")
		@Language("gradle")
		val script = kotlinPluginApply + """
			plugins {
				id("${pluginId}")
			}
		""".trimIndent()

		verifyTasksNotCreated(script)
	}

	@Test
	fun `android kotlin project doesn't create tasks when using all plugins`() {
		gradle.basedOn(GradleBuildTestResources.android)
		// Default build.gradle has the app plugin applied.
		gradle.buildFile.writeText(gradle.buildFile.readText().replace("id(\"com.android.application\")", ""))
		gradle.basedOn(GradleBuildTestResources.kotlin)
		gradle.file(settings, ContentMergeMode.MERGE_GRADLE, "settings.gradle.kts")

		val kotlinPluginApply = conditionalApplyKotlin("net.twisterrob.gradle.plugin.android-app") // :plugin
		@Language("gradle")
		val script = kotlinPluginApply + """
			plugins {
				// Android: id("net.twisterrob.gradle.plugin.android-library") // :plugin
				id("net.twisterrob.gradle.plugin.root") // :plugin:base
				id("net.twisterrob.gradle.plugin.java") // :plugin:languages
				id("net.twisterrob.gradle.plugin.java-library") // :plugin:languages
				id("net.twisterrob.gradle.plugin.kotlin") // :plugin:languages
				id("net.twisterrob.gradle.plugin.vcs") // :plugin:versioning
				id("net.twisterrob.gradle.plugin.quality") // :quality
				id("net.twisterrob.gradle.plugin.pmd") // :pmd
				id("net.twisterrob.gradle.plugin.checkstyle") // :checkstyle
				// Android: id("net.twisterrob.gradle.plugin.gradle.test") // :test
			}
		""".trimIndent()

		verifyTasksNotCreated(script)
	}

	private fun verifyTasksNotCreated(script: String) {
		val result = gradle.run(script, ":help").build()

		result.assertSuccess(":help")
	}

	private fun verifySingleTaskCreated(script: String, taskPath: String) {
		val result = gradle.run(script, ":help").buildAndFail()

		result.assertHasOutputLine("1 tasks created: $taskPath")
		result.assertHasOutputLine(
			Regex(
				"""$taskPath: java\.lang\.Exception: """
						+ """task '$taskPath' was created before root project '.*' was configured\."""
			)
		)
	}

	companion object {
		@Language("gradle.kts")
		val settings: String = """
			import java.io.PrintWriter
			import java.io.StringWriter
			
			val created: MutableMap<Task, Throwable> = mutableMapOf()
			gradle.taskGraph.whenReady {
				created.remove(created.keys.single { it.path == ":help" })
				if (created.isNotEmpty()) {
					val traces = created.entries.joinToString("\n") { (task, error) ->
						"${'$'}{task.path}: ${'$'}{error.stackTraceToString()}"
					}
					error("${'$'}{created.size} tasks created: ${'$'}{created.keys.joinToString { it.path }}\n${'$'}{traces}")
				}
			}
			gradle.beforeProject {
				val project = this
				project.tasks.configureEach {
					val task = this
					// Note: this will execute after whenReady in some cases, but it doesn't matter as no-one will read it.
					val message =  "${'$'}{task} was created before ${'$'}{project} was configured."
					created.put(task, Exception(message))
				}
			}
			
			/**
			 * Polyfill for Gradle <6.8 which doesn't have Kotlin 1.4+.
			 * [Throwable.stackTraceToString] is `@SinceKotlin("1.4")`.
			 * See https://docs.gradle.org/current/userguide/compatibility.html#kotlin
			 */
			fun Throwable.stackTraceToString(): String {
				val sw = StringWriter()
				this.printStackTrace(PrintWriter(sw, true))
				return sw.toString()
			}
		""".trimIndent()
	}

	/**
	 * Kotlin plugin had a dependency on what order it's applied in.
	 * The issue has been [nicely summarized](https://youtrack.jetbrains.com/issue/KT-44279)
	 * and [fixed](https://youtrack.jetbrains.com/issue/KT-46626) in Kotlin 1.5.30.
	 */
	private fun conditionalApplyKotlin(androidPluginId: String): String =
		if (KotlinVersion(1, 5, 30) <= KotlinVersions.UNDER_TEST) {
			// Location is not relevant since Kotlin 1.5.30, we can put this plugin in any location.
			"""
				plugins {
					id("org.jetbrains.kotlin.android")
					id("${androidPluginId}")
				}
				
			""".trimIndent() // Newline at end is important so that it can be prepended to other scripts.
		} else {
			// Location is relevant before Kotlin 1.5.30, we have to put this after the Android plugin.
			"""
				plugins {
					id("${androidPluginId}")
					id("org.jetbrains.kotlin.android")
				}
				
			""".trimIndent() // Newline at end is important so that it can be prepended to other scripts.
		}
}
