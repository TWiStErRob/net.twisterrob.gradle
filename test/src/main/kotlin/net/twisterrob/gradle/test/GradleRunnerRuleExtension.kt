package net.twisterrob.gradle.test

import org.gradle.testkit.runner.internal.DefaultGradleRunner
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor

open class GradleRunnerRuleExtension : TestInstancePostProcessor, BeforeEachCallback, AfterEachCallback {

	private val rule = object : GradleRunnerRule() {

		override val extraArgs: Array<String>
			get() = super.extraArgs + arrayOf(
				// https://docs.gradle.org/5.6/release-notes.html#fail-the-build-on-deprecation-warnings
				"--warning-mode=fail",
				"--init-script=runtime.init.gradle.kts",
				"--init-script=nagging.init.gradle.kts",
			)

		override fun setUp() {
			super.setUp()
			file(readResource("nagging.init.gradle.kts"), "nagging.init.gradle.kts")
			file(readResource("runtime.init.gradle.kts"), "runtime.init.gradle.kts")
			(runner as DefaultGradleRunner).withJvmArguments("-Xmx1g")
			//javaHome = File(System.getenv(System.getProperty("net.twisterrob.test.gradle.javaHomeEnv")))
		}

		private fun readResource(name: String): String {
			val initGradle = GradleRunnerRuleExtension::class.java.getResourceAsStream(name)
				?: error("Cannot find ${name} on classpath of ${GradleRunnerRuleExtension::class}")
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
