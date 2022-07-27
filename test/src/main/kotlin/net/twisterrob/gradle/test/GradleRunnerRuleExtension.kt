package net.twisterrob.gradle.test

import org.gradle.util.GradleVersion
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor

open class GradleRunnerRuleExtension : TestInstancePostProcessor, BeforeEachCallback, AfterEachCallback {

	private val rule = object : GradleRunnerRule() {
		override fun setUp() {
			super.setUp()
			extraArgs = DEFAULT_EXTRA_ARGS +
					if (gradleVersion < GradleVersion.version("5.6")) {
						emptyArray() // "fail" was not a valid option for --warning-mode before Gradle 5.6.
					} else {
						file(INIT_GRADLE_KTS.trimIndent(), "init.gradle.kts")
						// https://docs.gradle.org/5.6/release-notes.html#fail-the-build-on-deprecation-warnings
						arrayOf("--warning-mode=fail", "--init-script=init.gradle.kts")
					}
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

	companion object {
		@Language("gradle.kts")
		@Suppress("MaxLineLength")
		private const val INIT_GRADLE_KTS: String = """
			import org.gradle.util.GradleVersion
			
			/**
			 * Surgically ignoring messages like this will prevent actual executions from triggering
			 * stack traces and warnings, which means that even with some warnings,
			 * it's possible to use org.gradle.warning.mode=fail.
			 */
			fun doNotNagAbout(message: String) {
				val logger: Any = org.gradle.internal.deprecation.DeprecationLogger::class.java
					.getDeclaredField("DEPRECATED_FEATURE_HANDLER")
					.apply { isAccessible = true }
					.get(null)
			
				@Suppress("UNCHECKED_CAST")
				val messages: MutableSet<String> =
					org.gradle.internal.featurelifecycle.LoggingDeprecatedFeatureHandler::class.java
						.getDeclaredField("messages")
						.apply { isAccessible = true }
						.get(logger) as MutableSet<String>
			
				messages.add(message)
			}

			val agpVersion: String = System.getProperty("net.twisterrob.test.android.pluginVersion")
					?: error("Property 'net.twisterrob.test.android.pluginVersion' is not set.")
			if (GradleVersion.current().baseVersion == GradleVersion.version("7.4.2")) {
				if (Regex("^7\\.0\\.\\d$") matches agpVersion) {
					// net.twisterrob.test.android.pluginVersion=7.0.4
					// net.twisterrob.gradle.runner.gradleVersion=7.4.2
					// > Task :compileDebugRenderscript NO-SOURCE
					doNotNagAbout("Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property sourceDirs with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/7.4.2/userguide/upgrading_version_7.html#empty_directories_file_tree")
					// > Task :compileDebugAidl NO-SOURCE
					doNotNagAbout("Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property sourceFiles with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/7.4.2/userguide/upgrading_version_7.html#empty_directories_file_tree")
					// > Task :stripDebugDebugSymbols NO-SOURCE
					doNotNagAbout("Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property inputFiles with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/7.4.2/userguide/upgrading_version_7.html#empty_directories_file_tree")
					// > Task :bundleLibResDebug NO-SOURCE
					doNotNagAbout("Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property resources with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/7.4.2/userguide/upgrading_version_7.html#empty_directories_file_tree")
				}
				if (Regex("^4\\.2\\.\\d$") matches agpVersion) {
					// net.twisterrob.test.android.pluginVersion=4.2.2
					// net.twisterrob.gradle.runner.gradleVersion=7.4.2
					// > Task :mergeDebugNativeLibs NO-SOURCE
					doNotNagAbout("Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property projectNativeLibs with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/7.4.2/userguide/upgrading_version_7.html#empty_directories_file_tree")
				}
			}
			
			if (GradleVersion.current().baseVersion == GradleVersion.version("6.7.1")) {
				if (Regex("^4\\.0\\.\\d+$") matches agpVersion) {
					// AndroidVersionPluginIntgTest
					doNotNagAbout("Querying the mapped value of flatmap(provider(task 'calculateBuildConfigBuildTime', class net.twisterrob.gradle.android.tasks.CalculateBuildTimeTask)) before task ':calculateBuildConfigBuildTime' has completed has been deprecated. This will fail with an error in Gradle 7.0. Consult the upgrading guide for further information: https://docs.gradle.org/6.7.1/userguide/upgrading_version_6.html#querying_a_mapped_output_property_of_a_task_before_the_task_has_completed")
					doNotNagAbout("Querying the mapped value of flatmap(provider(task 'calculateBuildConfigVCSRevisionInfo', class net.twisterrob.gradle.android.tasks.CalculateVCSRevisionInfoTask)) before task ':calculateBuildConfigVCSRevisionInfo' has completed has been deprecated. This will fail with an error in Gradle 7.0. Consult the upgrading guide for further information: https://docs.gradle.org/6.7.1/userguide/upgrading_version_6.html#querying_a_mapped_output_property_of_a_task_before_the_task_has_completed")
					rootProject {
						//doNotNagAbout("Querying the mapped value of map(java.io.File property(org.gradle.api.file.Directory, fixed(class org.gradle.api.internal.file.DefaultFilePropertyFactory${"${'$'}"}FixedDirectory, ${'$'}{it.rootDir.resolve(\\"build/generated/ap_generated_sources/debug/out\\")})) org.gradle.api.internal.file.DefaultFilePropertyFactory${"${'$'}"}ToFileTransformer@6501aee0) before task ':compileDebugJavaWithJavac' has completed has been deprecated. This will fail with an error in Gradle 7.0. Consult the upgrading guide for further information: https://docs.gradle.org/6.7.1/userguide/upgrading_version_6.html#querying_a_mapped_output_property_of_a_task_before_the_task_has_completed")
					}
				}
			}
		"""
	}
}
