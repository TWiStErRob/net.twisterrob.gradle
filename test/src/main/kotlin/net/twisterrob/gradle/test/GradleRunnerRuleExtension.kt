package net.twisterrob.gradle.test

import org.gradle.util.GradleVersion
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor
import java.io.File

open class GradleRunnerRuleExtension : TestInstancePostProcessor, BeforeEachCallback, AfterEachCallback {

	private val rule = object : GradleRunnerRule() {
		override val extraArgs: Array<String>
			get() = super.extraArgs + arrayOf("--init-script=nagging.init.gradle.kts") + strictWarningMode()

		override fun setUp() {
			super.setUp()
			file(readInitGradle(), "nagging.init.gradle.kts")
			javaHome = File(System.getenv(System.getProperty("net.twisterrob.test.gradle.javaHomeEnv")))
		}

		private fun strictWarningMode(): Array<String> =
			if (GradleVersion.version("5.6") <= gradleVersion) {
				// https://docs.gradle.org/5.6/release-notes.html#fail-the-build-on-deprecation-warnings
				arrayOf("--warning-mode=fail")
			} else {
				emptyArray() // "fail" was not a valid option for --warning-mode before Gradle 5.6.
			}

		private fun readInitGradle(): String {
			val initGradle = GradleRunnerRuleExtension::class.java.getResourceAsStream("nagging.init.gradle.kts")
				?: error("Cannot find init.gradle.kts on classpath of ${GradleRunnerRuleExtension::class}")
			return initGradle.use { it.reader().readText() }
		}
	}

	override fun postProcessTestInstance(testInstance: Any, context: ExtensionContext) {
		testInstance
			.javaClass
			.declaredFields
			.filter { it.type === GradleRunnerRule::class.java }
			.onEach { it.isAccessible = true }
			.forEach { field -> field.set(testInstance, rule) }
	}

	override fun beforeEach(context: ExtensionContext) {
		rule.before()
	}

	override fun afterEach(context: ExtensionContext) {
		rule.after(!context.executionException.isPresent)
	}
}
