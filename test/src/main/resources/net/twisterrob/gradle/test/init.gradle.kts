import org.gradle.util.GradleVersion

/**
 * Surgically ignoring messages like this will prevent actual executions from triggering
 * stack traces and warnings, which means that even with some warnings,
 * it's possible to use org.gradle.warning.mode=fail.
 */
fun doNotNagAbout(message: String) {
	doNotNagAbout(Regex.fromLiteral(message))
}

fun doNotNagAbout(message: Regex) {
	// "fail" was not a valid option for --warning-mode before Gradle 5.6.0.
	// In Gradle 4.7.0 (c633542) org.gradle.util.SingleMessageLogger#deprecatedFeatureHandler came to be in a refactor.
	// In Gradle 6.2.0 it was split (247fd32) to org.gradle.util.DeprecationLogger#deprecatedFeatureHandler
	// and then further split (308086a) to org.gradle.internal.deprecation.DeprecationLogger#deprecatedFeatureHandler
	// and then renamed (a75aedd) to #DEPRECATED_FEATURE_HANDLER.
	val loggerField =
		if (GradleVersion.version("6.2.0") < GradleVersion.current().baseVersion) {
			Class.forName("org.gradle.internal.deprecation.DeprecationLogger")
				.getDeclaredField("DEPRECATED_FEATURE_HANDLER")
				.apply { isAccessible = true }
		} else {
			Class.forName("org.gradle.util.SingleMessageLogger")
				.getDeclaredField("deprecatedFeatureHandler")
				.apply { isAccessible = true }
		}
	val logger: Any = loggerField.get(null)

	// LoggingDeprecatedFeatureHandler#messages was added in Gradle 1.8.
	val messagesField = org.gradle.internal.featurelifecycle.LoggingDeprecatedFeatureHandler::class.java
		.getDeclaredField("messages")
		.apply { isAccessible = true }
	@Suppress("UNCHECKED_CAST")
	val messages: MutableSet<String> = messagesField.get(logger) as MutableSet<String>

	val ignore =
		if (messages is IgnoringSet) {
			messages as IgnoringSet
		} else {
			IgnoringSet(messages)
		}
	messagesField.set(logger, ignore)
	println("Ignoring deprecation: $message")
	ignore.ignorePattern(message)
}

private class IgnoringSet(
	private val backingSet: MutableSet<String>
) : MutableSet<String> by backingSet {

	private val ignores: MutableSet<Regex> = mutableSetOf()

	fun ignorePattern(regex: Regex) {
		ignores.add(regex)
	}

	override fun add(element: String): Boolean {
		val isIgnored = ignores.any { it.matches(element) }
		val isNew = backingSet.add(element)
		return !isIgnored && isNew
	}
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
	if (Regex("^3\\.6\\.\\d+$") matches agpVersion) {
		doNotNagAbout("Internal API constructor DefaultDomainObjectSet(Class<T>) has been deprecated. This is scheduled to be removed in Gradle 7.0. Please use ObjectFactory.domainObjectSet(Class<T>) instead. See https://docs.gradle.org/6.7.1/userguide/custom_gradle_types.html#domainobjectset for more details.")
		doNotNagAbout(
			Regex(
				"""Querying the mapped value of map\(java\.io\.File property\(org\.gradle\.api\.file\.Directory, property\(org\.gradle\.api\.file\.Directory, fixed\(class org\.gradle\.api\.internal\.file\.DefaultFilePropertyFactory\${'$'}FixedDirectory, .*\)\)\) org\.gradle\.api\.internal\.file\.DefaultFilePropertyFactory\${'$'}ToFileTransformer@[0-9a-f]{1,8}\) before task ':.*?compile(Debug|Release|.*)JavaWithJavac' has completed has been deprecated\. This will fail with an error in Gradle 7\.0\. Consult the upgrading guide for further information: https://docs\.gradle\.org/6\.7\.1/userguide/upgrading_version_6\.html#querying_a_mapped_output_property_of_a_task_before_the_task_has_completed"""
			)
		)
	}
	if (Regex("^4\\.0\\.\\d+$") matches agpVersion) {
		// AndroidVersionPluginIntgTest
		doNotNagAbout("Querying the mapped value of flatmap(provider(task 'calculateBuildConfigBuildTime', class net.twisterrob.gradle.android.tasks.CalculateBuildTimeTask)) before task ':calculateBuildConfigBuildTime' has completed has been deprecated. This will fail with an error in Gradle 7.0. Consult the upgrading guide for further information: https://docs.gradle.org/6.7.1/userguide/upgrading_version_6.html#querying_a_mapped_output_property_of_a_task_before_the_task_has_completed")
		doNotNagAbout("Querying the mapped value of flatmap(provider(task 'calculateBuildConfigVCSRevisionInfo', class net.twisterrob.gradle.android.tasks.CalculateVCSRevisionInfoTask)) before task ':calculateBuildConfigVCSRevisionInfo' has completed has been deprecated. This will fail with an error in Gradle 7.0. Consult the upgrading guide for further information: https://docs.gradle.org/6.7.1/userguide/upgrading_version_6.html#querying_a_mapped_output_property_of_a_task_before_the_task_has_completed")
		doNotNagAbout(
			Regex(
				"""Querying the mapped value of map\(java\.io\.File property\(org\.gradle\.api\.file\.Directory, fixed\(class org\.gradle\.api\.internal\.file\.DefaultFilePropertyFactory\${'$'}FixedDirectory, .*\)\) org\.gradle\.api\.internal\.file\.DefaultFilePropertyFactory\${'$'}ToFileTransformer@[0-9a-f]{1,8}\) before task ':.*?compile(Debug|Release|.*)JavaWithJavac' has completed has been deprecated\. This will fail with an error in Gradle 7\.0\. Consult the upgrading guide for further information: https://docs\.gradle\.org/6\.7\.1/userguide/upgrading_version_6\.html#querying_a_mapped_output_property_of_a_task_before_the_task_has_completed"""
			)
		)
	}
}
