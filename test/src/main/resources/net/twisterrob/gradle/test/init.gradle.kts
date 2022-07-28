import org.gradle.util.GradleVersion

/**
 * Surgically ignoring messages like this will prevent actual executions from triggering
 * stack traces and warnings, which means that even with some warnings,
 * it's possible to use org.gradle.warning.mode=fail.
 */
fun doNotNagAbout(gradle: String, agpRegex: String, message: String) {
	if (GradleVersion.current().baseVersion == GradleVersion.version(gradle)) {
		if (Regex(agpRegex) matches agpVersion) {
			_doNotNagAboutPattern(Regex.fromLiteral(message))
		}
	}
}

fun doNotNagAboutPattern(gradle: String, agpRegex: String, messageRegex: String) {
	if (GradleVersion.current().baseVersion == GradleVersion.version(gradle)) {
		if (Regex(agpRegex) matches agpVersion) {
			_doNotNagAboutPattern(Regex(messageRegex))
		}
	}
}

rootProject {
	// Groovy .ext === Kotlin .extensions.extraProperties === Kotlin DSL .extra
	// Based on https://stackoverflow.com/a/19269037/253468
	// Based on https://discuss.gradle.org/t/how-to-access-a-function-defined-in-init-gradle-in-build-script/6200/2

	// Access from build.gradle:
	// def doNotNagAbout = rootProject.ext["doNotNagAbout"]
	// doNotNagAbout("7.4.2", "^7\\.2\\.\\d+\$", "message")

	// Access from build.gradle.kts:
	// val doNotNagAbout = project.rootProject.extra["doNotNagAbout"] as (String, String, String) -> Unit
	// val doNotNagAbout = project.rootProject.extensions.extraProperties["doNotNagAbout"] as (String, String, String) -> Unit
	// doNotNagAbout("7.4.2", """^7\.2\.\d$""", "message")

	extensions.extraProperties.set("doNotNagAbout", ::doNotNagAbout)
	extensions.extraProperties.set("doNotNagAboutPattern", ::doNotNagAboutPattern)
}

fun _doNotNagAboutPattern(message: Regex) {
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

	val ignore = if (messages is IgnoringSet) messages else IgnoringSet(messages)
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
// Sorted by (Gradle, AGP) below here.

doNotNagAbout(
	"6.7.1",
	"""^3\.5\.\d$""",
	"The BuildListener.buildStarted(Gradle) method has been deprecated. This is scheduled to be removed in Gradle 7.0. Consult the upgrading guide for further information: https://docs.gradle.org/6.7.1/userguide/upgrading_version_5.html#apis_buildlistener_buildstarted_and_gradle_buildstarted_have_been_deprecated"
)
doNotNagAbout(
	"6.7.1",
	"""^(3\.5\.\d|3\.6\.\d)$""",
	"Internal API constructor DefaultDomainObjectSet(Class<T>) has been deprecated. This is scheduled to be removed in Gradle 7.0. Please use ObjectFactory.domainObjectSet(Class<T>) instead. See https://docs.gradle.org/6.7.1/userguide/custom_gradle_types.html#domainobjectset for more details."
)
doNotNagAboutPattern(
	"6.7.1",
	"""^3\.6\.\d+$""",
	"""Querying the mapped value of map\(java\.io\.File property\(org\.gradle\.api\.file\.Directory, property\(org\.gradle\.api\.file\.Directory, fixed\(class org\.gradle\.api\.internal\.file\.DefaultFilePropertyFactory\${'$'}FixedDirectory, .*\)\)\) org\.gradle\.api\.internal\.file\.DefaultFilePropertyFactory\${'$'}ToFileTransformer@[0-9a-f]{1,8}\) before task ':.*?compile(Debug|Release|.*)JavaWithJavac' has completed has been deprecated\. This will fail with an error in Gradle 7\.0\. Consult the upgrading guide for further information: https://docs\.gradle\.org/6\.7\.1/userguide/upgrading_version_6\.html#querying_a_mapped_output_property_of_a_task_before_the_task_has_completed"""
)
doNotNagAboutPattern(
	"6.7.1",
	"""^4\.0\.\d$""",
	// e.g. AndroidVersionPluginIntgTest
	"""Querying the mapped value of flatmap\(provider\(task 'calculateBuildConfigBuildTime', class net\.twisterrob\.gradle\.android\.tasks\.CalculateBuildTimeTask\)\) before task '(:.+)?:calculateBuildConfigBuildTime' has completed has been deprecated\. This will fail with an error in Gradle 7\.0\. Consult the upgrading guide for further information: https://docs\.gradle\.org/6\.7\.1/userguide/upgrading_version_6\.html#querying_a_mapped_output_property_of_a_task_before_the_task_has_completed"""
)
doNotNagAboutPattern(
	"6.7.1",
	"""^4\.0\.\d$""",
	// e.g. AndroidVersionPluginIntgTest
	"""Querying the mapped value of flatmap\(provider\(task 'calculateBuildConfigVCSRevisionInfo', class net\.twisterrob\.gradle\.android\.tasks\.CalculateVCSRevisionInfoTask\)\) before task '(:.+)?:calculateBuildConfigVCSRevisionInfo' has completed has been deprecated\. This will fail with an error in Gradle 7\.0\. Consult the upgrading guide for further information: https://docs\.gradle\.org/6\.7\.1/userguide/upgrading_version_6\.html#querying_a_mapped_output_property_of_a_task_before_the_task_has_completed"""
)
doNotNagAboutPattern(
	"6.7.1",
	"""^4\.0\.\d$""",
	"""Querying the mapped value of map\(java\.io\.File property\(org\.gradle\.api\.file\.Directory, fixed\(class org\.gradle\.api\.internal\.file\.DefaultFilePropertyFactory\${'$'}FixedDirectory, .*\)\) org\.gradle\.api\.internal\.file\.DefaultFilePropertyFactory\${'$'}ToFileTransformer@[0-9a-f]{1,8}\) before task ':.*?compile(Debug|Release|.*)JavaWithJavac' has completed has been deprecated\. This will fail with an error in Gradle 7\.0\. Consult the upgrading guide for further information: https://docs\.gradle\.org/6\.7\.1/userguide/upgrading_version_6\.html#querying_a_mapped_output_property_of_a_task_before_the_task_has_completed"""
)
doNotNagAbout(
	"7.4.2",
	"""^4\.2\.\d$""",
	// > Task :mergeDebugNativeLibs NO-SOURCE
	"Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property projectNativeLibs with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/7.4.2/userguide/upgrading_version_7.html#empty_directories_file_tree"
)
doNotNagAbout(
	"7.4.2",
	"""^(4\.2\.\d|7\.0\.\d)$""",
	// > Task :compileDebugRenderscript NO-SOURCE
	"Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property sourceDirs with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/7.4.2/userguide/upgrading_version_7.html#empty_directories_file_tree"
)
doNotNagAbout(
	"7.4.2",
	"""^(4\.2\.\d|7\.0\.\d)$""",
	// > Task :compileDebugAidl NO-SOURCE
	"Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property sourceFiles with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/7.4.2/userguide/upgrading_version_7.html#empty_directories_file_tree"
)
doNotNagAbout(
	"7.4.2",
	"""^(4\.2\.\d|7\.0\.\d)$""",
	// > Task :stripDebugDebugSymbols NO-SOURCE
	"Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property inputFiles with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/7.4.2/userguide/upgrading_version_7.html#empty_directories_file_tree"
)
doNotNagAbout(
	"7.4.2",
	"""^(4\.2\.\d|7\.0\.\d)$""",
	// > Task :bundleLibResDebug NO-SOURCE
	"Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property resources with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/7.4.2/userguide/upgrading_version_7.html#empty_directories_file_tree"
)
