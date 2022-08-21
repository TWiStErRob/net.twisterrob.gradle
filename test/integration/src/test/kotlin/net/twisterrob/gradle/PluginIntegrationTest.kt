package net.twisterrob.gradle

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
		gradle.file(settings, "settings.gradle.kts")

		@Language("gradle")
		val script = """
			task eagerlyCreated { }
		""".trimIndent()

		verifySingleTaskCreated(script, ":eagerlyCreated")
	}

	/**
	 * This is a meta-test, it's verifying the code in settings.gradle.kts that it's capable of flagging bad behavior.
	 */
	@Test
	fun `created task is detected`() {
		gradle.file(settings, "settings.gradle.kts")

		@Language("gradle")
		val script = """
			tasks.create("eagerlyCreated") { }
		""".trimIndent()

		verifySingleTaskCreated(script, ":eagerlyCreated")
	}

	/**
	 * This is a meta-test, it's verifying the code in settings.gradle.kts that it's capable of flagging bad behavior.
	 */
	@Test
	fun `registered task is not detected`() {
		gradle.file(settings, "settings.gradle.kts")

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
		gradle.file(settings, "settings.gradle.kts")

		@Language("gradle")
		val script = """
			def task = tasks.register("lazilyRegistered") { }
			task.get()
		""".trimIndent()

		verifySingleTaskCreated(script, ":lazilyRegistered")
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(
		strings = [
			"net.twisterrob.root", // :plugin:base
			"net.twisterrob.java", // :plugin:languages
			"net.twisterrob.java-library", // :plugin:languages
			// Kotlin: "net.twisterrob.kotlin", // :plugin:languages
			"net.twisterrob.vcs", // :plugin:versioning
			// Android: "net.twisterrob.android-app", // :plugin
			// Android: "net.twisterrob.android-library", // :plugin
			// Android: "net.twisterrob.android-test", // :plugin
			"net.twisterrob.quality", // :quality
			"net.twisterrob.pmd", // :pmd
			"net.twisterrob.checkstyle", // :checkstyle
			"net.twisterrob.gradle.test", // :test
		]
	)
	fun `empty project doesn't create tasks when using plugin`(pluginId: String) {
		gradle.file(settings, "settings.gradle.kts")

		@Language("gradle")
		val script = """
			apply plugin: "${pluginId}"
		""".trimIndent()

		verifyTasksNotCreated(script)
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(
		strings = [
			"net.twisterrob.root", // :plugin:base
			"net.twisterrob.java", // :plugin:languages
			"net.twisterrob.java-library", // :plugin:languages
			"net.twisterrob.kotlin", // :plugin:languages
			"net.twisterrob.vcs", // :plugin:versioning
			// Android: "net.twisterrob.android-app", // :plugin
			// Android: "net.twisterrob.android-library", // :plugin
			// Android: "net.twisterrob.android-test", // :plugin
			"net.twisterrob.quality", // :quality
			"net.twisterrob.pmd", // :pmd
			"net.twisterrob.checkstyle", // :checkstyle
			"net.twisterrob.gradle.test", // :test
		]
	)
	fun `kotlin project doesn't create tasks when using plugin`(pluginId: String) {
		gradle.file(settings, "settings.gradle.kts")
		gradle.basedOn(GradleBuildTestResources.kotlin)

		@Language("gradle")
		val script = """
			apply plugin: "${pluginId}"
		""".trimIndent()

		verifyTasksNotCreated(script)
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(
		strings = [
			"net.twisterrob.root", // :plugin:base
			"net.twisterrob.java", // :plugin:languages
			"net.twisterrob.java-library", // :plugin:languages
			// Kotlin: "net.twisterrob.kotlin", // :plugin:languages
			"net.twisterrob.vcs", // :plugin:versioning
			"net.twisterrob.quality", // :quality
			"net.twisterrob.pmd", // :pmd
			"net.twisterrob.checkstyle", // :checkstyle
			// Android: "net.twisterrob.gradle.test", // :test
		]
	)
	fun `android app project doesn't create tasks when using plugin`(pluginId: String) {
		gradle.file(settings, "settings.gradle.kts")
		gradle.basedOn(GradleBuildTestResources.android)

		@Language("gradle")
		val script = """
			apply plugin: "net.twisterrob.android-app"
			apply plugin: "${pluginId}"
		""".trimIndent()

		verifyTasksNotCreated(script)
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(
		strings = [
			"net.twisterrob.root", // :plugin:base
			"net.twisterrob.java", // :plugin:languages
			"net.twisterrob.java-library", // :plugin:languages
			// Kotlin: "net.twisterrob.kotlin", // :plugin:languages
			"net.twisterrob.vcs", // :plugin:versioning
			"net.twisterrob.quality", // :quality
			"net.twisterrob.pmd", // :pmd
			"net.twisterrob.checkstyle", // :checkstyle
			// Android: "net.twisterrob.gradle.test", // :test
		]
	)
	fun `android library project doesn't create tasks when using plugin`(pluginId: String) {
		gradle.file(settings, "settings.gradle.kts")
		gradle.basedOn(GradleBuildTestResources.android)

		@Language("gradle")
		val script = """
			apply plugin: "net.twisterrob.android-library"
			apply plugin: "${pluginId}"
		""".trimIndent()

		verifyTasksNotCreated(script)
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(
		strings = [
			"net.twisterrob.root", // :plugin:base
			"net.twisterrob.java", // :plugin:languages
			"net.twisterrob.java-library", // :plugin:languages
			"net.twisterrob.kotlin", // :plugin:languages
			"net.twisterrob.vcs", // :plugin:versioning
			// Android: "net.twisterrob.android-app", // :plugin
			// Android: "net.twisterrob.android-library", // :plugin
			// Special: "net.twisterrob.android-test", // :plugin
			"net.twisterrob.quality", // :quality
			"net.twisterrob.pmd", // :pmd
			"net.twisterrob.checkstyle", // :checkstyle
			// Android: "net.twisterrob.gradle.test", // :test
		]
	)
	fun `android kotlin app project doesn't create tasks when using plugin`(pluginId: String) {
		gradle.file(settings, "settings.gradle.kts")
		gradle.basedOn(GradleBuildTestResources.android)
		gradle.basedOn(GradleBuildTestResources.kotlin)

		@Language("gradle")
		val script = """
			apply plugin: "kotlin-android"
			apply plugin: "net.twisterrob.android-app"
			apply plugin: "${pluginId}"
		""".trimIndent()

		verifyTasksNotCreated(script)
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(
		strings = [
			"net.twisterrob.root", // :plugin:base
			"net.twisterrob.java", // :plugin:languages
			"net.twisterrob.java-library", // :plugin:languages
			"net.twisterrob.kotlin", // :plugin:languages
			"net.twisterrob.vcs", // :plugin:versioning
			// Android: "net.twisterrob.android-app", // :plugin
			// Android: "net.twisterrob.android-library", // :plugin
			// Special: "net.twisterrob.android-test", // :plugin
			"net.twisterrob.quality", // :quality
			"net.twisterrob.pmd", // :pmd
			"net.twisterrob.checkstyle", // :checkstyle
			// Android: "net.twisterrob.gradle.test", // :test
		]
	)
	fun `android kotlin library project doesn't create tasks when using plugin`(pluginId: String) {
		gradle.file(settings, "settings.gradle.kts")
		gradle.basedOn(GradleBuildTestResources.android)
		gradle.basedOn(GradleBuildTestResources.kotlin)

		@Language("gradle")
		val script = """
			apply plugin: "kotlin-android"
			apply plugin: "net.twisterrob.android-library"
			apply plugin: "${pluginId}"
		""".trimIndent()

		verifyTasksNotCreated(script)
	}

	@Test
	fun `android kotlin project doesn't create tasks when using all plugins`() {
		gradle.file(settings, "settings.gradle.kts")
		gradle.basedOn(GradleBuildTestResources.android)
		gradle.basedOn(GradleBuildTestResources.kotlin)

		@Language("gradle")
		val script = """
			apply plugin: "kotlin-android"
			apply plugin: "net.twisterrob.android-app" // :plugin
			// Android: apply plugin: "net.twisterrob.android-library" // :plugin
			apply plugin: "net.twisterrob.root" // :plugin:base
			apply plugin: "net.twisterrob.java" // :plugin:languages
			apply plugin: "net.twisterrob.java-library" // :plugin:languages
			apply plugin: "net.twisterrob.kotlin" // :plugin:languages
			apply plugin: "net.twisterrob.vcs" // :plugin:versioning
			apply plugin: "net.twisterrob.quality" // :quality
			apply plugin: "net.twisterrob.pmd" // :pmd
			apply plugin: "net.twisterrob.checkstyle" // :checkstyle
			// Android: apply plugin: "net.twisterrob.gradle.test" // :test
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
		""".trimIndent()
	}
}
