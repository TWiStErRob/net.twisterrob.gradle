package net.twisterrob.gradle

import net.twisterrob.gradle.common.AGPVersions
import net.twisterrob.gradle.common.KotlinVersions
import net.twisterrob.gradle.test.GradleBuildTestResources
import net.twisterrob.gradle.test.GradleBuildTestResources.basedOn
import net.twisterrob.gradle.test.GradleRunnerRule
import net.twisterrob.gradle.test.GradleRunnerRuleExtension
import net.twisterrob.gradle.test.assertHasOutputLine
import net.twisterrob.gradle.test.assertSuccess
import net.twisterrob.gradle.test.fixtures.ContentMergeMode
import org.gradle.util.GradleVersion
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 * See all `gradlePlugin { plugins { create() { id =` blocks in this project's build.gradle.kts files.
 */
@Suppress("detekt.PropertyUsedBeforeDeclaration") // settings is a lazy property.
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

	private val settings: String
		@Language("gradle.kts")
		get() = """
			import java.io.PrintWriter
			import java.io.StringWriter
			
			val created: MutableMap<Task, Throwable> = mutableMapOf()
			gradle.taskGraph.whenReady {
				created.remove(created.keys.single { it.path == ":help" }) // Must be always there.
				/*@formatter:off*/
				val realizedTasks = listOf(${'\n'}${
					calculateExceptionallyRealizedTasks()
						.joinToString(separator = "\n") { """"${it}",""" }
						.prependIndent("\t\t\t\t\t")
				}
				)
				/*@formatter:on*/
				created.keys.filter { it.path in realizedTasks }.forEach(created::remove)
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

	private fun calculateExceptionallyRealizedTasks(): List<String> {
		val generalTasks: List<String> = listOf(
			":help",
		)
		val agpTasks: List<String> =
			if (AGPVersions.UNDER_TEST compatible AGPVersions.v72x) {
				// Known bad tasks on AGP 7.2: old version, situation unlikely to change.
				listOf(
					":compileDebugRenderscript",
					":compileReleaseRenderscript",
				)
			} else {
				emptyList()
			}
		val kgpTasks: List<String> =
			if (KotlinVersions.UNDER_TEST.inRange(KotlinVersions.v1720, KotlinVersions.v190)) {
				// https://youtrack.jetbrains.com/issue/KT-54468
				// Known bad tasks on Kotlin 1.7.20-1.8.21 (fixed in 1.9.0):
				// with K2 ongoing, situation unlikely to change.
				listOf(
					":compileDebugKotlin",
					":compileReleaseKotlin",
					":compileDebugUnitTestKotlin",
					":compileReleaseUnitTestKotlin",
					":compileDebugAndroidTestKotlin",
				)
			} else {
				emptyList()
			}
		val gradleTasks: List<String> =
			if (KotlinVersions.UNDER_TEST < KotlinVersions.v200) {
				// This only affects `kotlin project doesn't create tasks when using plugin`(String) test.
				// https://youtrack.jetbrains.com/issue/KT-60664 fixed in 2.0.0-Beta4.
				// (originally: https://github.com/gradle/gradle/issues/25841)
				val minorVersion = gradle.gradleVersion.baseVersion
					.version
					.replace("""(\d+\.\d+)\.\d+""".toRegex(), "$1")
				@Suppress("UseIfInsteadOfWhen") // It's easier to read and maintain this way.
				when (GradleVersion.version(minorVersion)) {
					in GradleVersion.version("8.3")..GradleVersion.version("8.10") -> listOf(
						":compileJava",
					)
					else -> emptyList()
				}
			} else {
				emptyList()
			}
		return generalTasks + agpTasks + kgpTasks + gradleTasks
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

@Suppress("UnusedReceiverParameter") // To make it only available through the object.
private val KotlinVersions.v1720: KotlinVersion get() = KotlinVersion(1, 7, 20)
@Suppress("UnusedReceiverParameter") // To make it only available through the object.
private val KotlinVersions.v190: KotlinVersion get() = KotlinVersion(1, 9, 0)
@Suppress("UnusedReceiverParameter") // To make it only available through the object.
private val KotlinVersions.v200: KotlinVersion get() = KotlinVersion(2, 0, 0)

private fun KotlinVersion.inRange(fromInclusive: KotlinVersion, toExcl: KotlinVersion): Boolean =
	fromInclusive <= this && this < toExcl
