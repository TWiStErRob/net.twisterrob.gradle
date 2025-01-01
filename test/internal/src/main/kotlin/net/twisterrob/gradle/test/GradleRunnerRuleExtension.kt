package net.twisterrob.gradle.test

import net.twisterrob.gradle.test.testkit.withJvmArguments
import org.gradle.launcher.daemon.configuration.DaemonParameters
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor

class GradleRunnerRuleExtension : TestInstancePostProcessor, BeforeEachCallback, AfterEachCallback {

	private val rule = object : GradleRunnerRule() {

		override val extraArgs: Array<String>
			get() = super.extraArgs + arrayOf(
				// https://docs.gradle.org/5.6/release-notes.html#fail-the-build-on-deprecation-warnings
				"-Dorg.gradle.warning.mode=fail", // Allowing individual tests to override with --warning-mode.
				"--init-script=runtime.init.gradle.kts",
				"--init-script=nagging.init.gradle.kts",
			)

		override fun setUp() {
			super.setUp()
			file(readResource("nagging.init.gradle.kts"), "nagging.init.gradle.kts")
			file(readResource("runtime.init.gradle.kts"), "runtime.init.gradle.kts")
			configureMemory()
			//javaHome = File(System.getenv(System.getProperty("net.twisterrob.test.gradle.javaHomeEnv")))
		}

		/**
		 * I've had enough of Gradle OOMing on me in tests.
		 *
		 * In the recent year I've spent countless hour diagnosing and figuring out what's wrong with my CI.
		 * It turns out it was running slow, timing out, `java.lang.OutOfMemoryError: Metaspace`,
		 * "The Daemon will expire after the build after running out of JVM Metaspace.", and similar problems,
		 * because the default Gradle config is just not enough for running AGP/Kotlin.
		 * See also [R8 Metaspace increase](https://github.com/TWiStErRob/net.twisterrob.gradle/issues/147).
		 *
		 * The final straw was switching to `withPluginClasspath()` with `plugins { }`,
		 * which apparently consumes more memory than downloading from `repositories { }` and using `apply plugin:`.
		 *
		 * So this is a workaround for [Gradle runs out of metaspace](https://github.com/gradle/gradle/issues/23698).
		 * Abusing how [`org.gradle.jvmargs` is not merged](https://github.com/gradle/gradle/issues/19750),
		 * so not setting Metaspace will make it unlimited.
		 *
		 * The heap size set here is following the default in [DaemonParameters.DEFAULT_JVM_ARGS] as of Gradle 8.0.
		 */
		private fun configureMemory() {
			runner.withJvmArguments("-Xmx512M")
		}

		private fun readResource(name: String): String {
			val initGradle = GradleRunnerRuleExtension::class.java.getResourceAsStream(name)
				?: error("Cannot find ${name} on classpath of ${GradleRunnerRuleExtension::class}")
			return initGradle.use { it.reader().readText() }
		}

		/**
		 * Expose [GradleRunnerRule.before] to the outer class.
		 */
		fun beforeEach() {
			super.before()
		}

		/**
		 * Expose [GradleRunnerRule.before] to the outer class.
		 */
		fun afterEach(success: Boolean) {
			super.after(success)
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
		rule.beforeEach()
	}

	override fun afterEach(context: ExtensionContext) {
		rule.afterEach(!context.executionException.isPresent)
	}
}
