import net.twisterrob.gradle.nagging.doNotNagAbout
import net.twisterrob.gradle.nagging.doNotNagAboutPattern
import org.gradle.util.GradleVersion
import java.io.File

initscript {
	dependencies {
		// Essentially:
		// classpath(files("test\\internal\\runtime\\build\\libs\\runtime-0.15-SNAPSHOT.jar"))
		val initscriptMetadata = File(System.getProperty("net.twisterrob.gradle.test.initscript-runtime"))
		java.util.Properties()
			.apply { load(initscriptMetadata.reader()) }
			.getProperty("initscript-classpath")
			.split(File.pathSeparator)
			.forEach { add("classpath", files(it)) }
	}
}

apply<net.twisterrob.gradle.nagging.NaggingPlugin>()

// Below nagging suppressions are sorted by (Gradle version, AGP version) lexicographically.

doNotNagAbout(
	"6.7.1",
	"""^3\.5\.\d$""",
	// There were about 33 failures of apply plugin: 'com.android.library' in various tests.
	// at org.gradle.invocation.DefaultGradle.addBuildListener(DefaultGradle.java:422)
	// at com.android.build.gradle.internal.BuildSessionImpl.initialize(BuildSessionImpl.java:156)
	// at com.android.build.gradle.internal.PluginInitializer.initialize(PluginInitializer.java:88)
	// at com.android.build.gradle.BasePlugin.basePluginApply(BasePlugin.java:236)
	// at com.android.build.gradle.internal.crash.CrashReporting.runAction(crash_reporting.kt:27)
	// at com.android.build.gradle.BasePlugin.apply(BasePlugin.java:119)
	"The BuildListener.buildStarted(Gradle) method has been deprecated. This is scheduled to be removed in Gradle 7.0. Consult the upgrading guide for further information: https://docs.gradle.org/6.7.1/userguide/upgrading_version_5.html#apis_buildlistener_buildstarted_and_gradle_buildstarted_have_been_deprecated"
)
doNotNagAbout(
	"6.7.1",
	"""^3\.5\.\d$""",
	// There were about 17 failures of Task :checkDebugManifest in various tests.
	"Property 'manifest' has @Input annotation used on property of type 'File'. This behaviour has been deprecated and is scheduled to be removed in Gradle 7.0. See https://docs.gradle.org/6.7.1/userguide/more_about_tasks.html#sec:up_to_date_checks for more details."
)
doNotNagAbout(
	"6.7.1",
	"""^3\.5\.\d$""",
	// There were about 35 failures of Task :compileDebugJavaWithJavac in various tests.
	"Extending the JavaCompile task has been deprecated. This is scheduled to be removed in Gradle 7.0. Configure the task instead."
)
doNotNagAbout(
	"6.7.1",
	"""^3\.5\.\d$""",
	// There were about 15 failures of Task :lint in various tests.
	"Property 'lintOptions' is not annotated with an input or output annotation. This behaviour has been deprecated and is scheduled to be removed in Gradle 7.0. See https://docs.gradle.org/6.7.1/userguide/more_about_tasks.html#sec:up_to_date_checks for more details."
)
doNotNagAbout(
	"6.7.1",
	"""^3\.5\.\d$""",
	// There were about 35 failures of Task :packageDebugResources in various tests.
	"Property 'resourcesComputer' is not annotated with an input or output annotation. This behaviour has been deprecated and is scheduled to be removed in Gradle 7.0. See https://docs.gradle.org/6.7.1/userguide/more_about_tasks.html#sec:up_to_date_checks for more details."
)
doNotNagAbout(
	"6.7.1",
	"""^3\.5\.\d$""",
	// There were about 25 failures of Task :generateReleaseRFile and Task :processDebugResources in various tests.
	"Property 'manifestFile' is not annotated with an input or output annotation. This behaviour has been deprecated and is scheduled to be removed in Gradle 7.0. See https://docs.gradle.org/6.7.1/userguide/more_about_tasks.html#sec:up_to_date_checks for more details."
)
doNotNagAboutPattern( // Task path might be in sub-module, memory address of transformer changes, task name changes based on variant.
	"6.7.1",
	"""^3\.5\.\d$""",
	// There were about 50 failures of Task :generateReleaseRFile and Task :generateDebugRFile
	// and Task :javaPreCompileDebug and Task :javaPreCompileRelease in various tests.
	"""Querying the mapped value of map\(java.io.File task '(:.+?)?:compile(Debug|Release|.+)JavaWithJavac' property 'annotationProcessorSourcesDirectory' org.gradle.api.internal.file.DefaultFilePropertyFactory\${'$'}ToFileTransformer@[0-9a-f]{1,8}\) before task '(:.+?)?:compile(Debug|Release|.+)JavaWithJavac' has completed has been deprecated. This will fail with an error in Gradle 7.0. Consult the upgrading guide for further information: https://docs.gradle.org/6.7.1/userguide/upgrading_version_6.html#querying_a_mapped_output_property_of_a_task_before_the_task_has_completed"""
)
doNotNagAbout(
	"6.7.1",
	"""^3\.5\.\d$""",
	// There were about 14 failures of Task :generateReleaseRFile in various tests.
	"Property 'sourceOutputDir' is not annotated with an input or output annotation. This behaviour has been deprecated and is scheduled to be removed in Gradle 7.0. See https://docs.gradle.org/6.7.1/userguide/more_about_tasks.html#sec:up_to_date_checks for more details."
)
doNotNagAbout(
	"6.7.1",
	"""^3\.5\.\d$""",
	// There were about 17 failures of Task :parseDebugLibraryResources in various tests.
	"Injecting the input artifact of a transform as a File has been deprecated. This is scheduled to be removed in Gradle 7.0. Declare the input artifact as Provider<FileSystemLocation> instead. See https://docs.gradle.org/6.7.1/userguide/artifact_transforms.html#sec:implementing-artifact-transforms for more details."
)
doNotNagAbout(
	"6.7.1",
	"""^3\.5\.\d$""",
	// There were about 24 failures of Task :createDebugCompatibleScreenManifests in various tests.
	"Property 'outputScope' is not annotated with an input or output annotation. This behaviour has been deprecated and is scheduled to be removed in Gradle 7.0. See https://docs.gradle.org/6.7.1/userguide/more_about_tasks.html#sec:up_to_date_checks for more details."
)
doNotNagAbout(
	"6.7.1",
	"""^3\.5\.\d$""",
	// There were about 16 failures of Task :processDebugManifest in various tests.
	"Type 'ProcessApplicationManifest': static method 'getArtifactName()' should not be annotated with: @Internal. This behaviour has been deprecated and is scheduled to be removed in Gradle 7.0. See https://docs.gradle.org/6.7.1/userguide/more_about_tasks.html#sec:up_to_date_checks for more details."
)
doNotNagAbout(
	"6.7.1",
	"""^3\.5\.\d$""",
	// There were about 16 failures of Task :processDebugManifest in various tests.
	"Type 'ProcessApplicationManifest': static method 'getNameFromAutoNamespacedManifest()' should not be annotated with: @Internal. This behaviour has been deprecated and is scheduled to be removed in Gradle 7.0. See https://docs.gradle.org/6.7.1/userguide/more_about_tasks.html#sec:up_to_date_checks for more details."
)
doNotNagAbout(
	"6.7.1",
	"""^3\.5\.\d$""",
	// There were about 25 failures of Task :processDebugResources and :processReleaseResources in various tests.
	"Type 'LinkApplicationAndroidResourcesTask': non-property method 'canHaveSplits()' should not be annotated with: @Input. This behaviour has been deprecated and is scheduled to be removed in Gradle 7.0. See https://docs.gradle.org/6.7.1/userguide/more_about_tasks.html#sec:up_to_date_checks for more details."
)
doNotNagAbout(
	"6.7.1",
	"""^(3\.5\.\d|3\.6\.\d)$""",
	"Internal API constructor DefaultDomainObjectSet(Class<T>) has been deprecated. This is scheduled to be removed in Gradle 7.0. Please use ObjectFactory.domainObjectSet(Class<T>) instead. See https://docs.gradle.org/6.7.1/userguide/custom_gradle_types.html#domainobjectset for more details."
)
doNotNagAboutPattern( // Task path might be in sub-module, memory address of transformer changes, task name changes based on variant.
	"6.7.1",
	"""^3\.6\.\d+$""",
	"""Querying the mapped value of map\(java\.io\.File property\(org\.gradle\.api\.file\.Directory, property\(org\.gradle\.api\.file\.Directory, fixed\(class org\.gradle\.api\.internal\.file\.DefaultFilePropertyFactory\${'$'}FixedDirectory, .*\)\)\) org\.gradle\.api\.internal\.file\.DefaultFilePropertyFactory\${'$'}ToFileTransformer@[0-9a-f]{1,8}\) before task '(:.+?)?:compile(Debug|Release|.+)JavaWithJavac' has completed has been deprecated\. This will fail with an error in Gradle 7\.0\. Consult the upgrading guide for further information: https://docs\.gradle\.org/6\.7\.1/userguide/upgrading_version_6\.html#querying_a_mapped_output_property_of_a_task_before_the_task_has_completed"""
)
doNotNagAboutPattern( // Task path might be in sub-module, memory address of transformer changes, task name changes based on variant.
	"6.7.1",
	"""^4\.0\.\d$""",
	"""Querying the mapped value of map\(java\.io\.File property\(org\.gradle\.api\.file\.Directory, fixed\(class org\.gradle\.api\.internal\.file\.DefaultFilePropertyFactory\${'$'}FixedDirectory, .*\)\) org\.gradle\.api\.internal\.file\.DefaultFilePropertyFactory\${'$'}ToFileTransformer@[0-9a-f]{1,8}\) before task '(:.+?)?:compile(Debug|Release|.+)JavaWithJavac' has completed has been deprecated\. This will fail with an error in Gradle 7\.0\. Consult the upgrading guide for further information: https://docs\.gradle\.org/6\.7\.1/userguide/upgrading_version_6\.html#querying_a_mapped_output_property_of_a_task_before_the_task_has_completed"""
)
doNotNagAboutPattern( // Task path might be in sub-module.
	"6.7.1",
	"""^4\.0\.\d$""",
	// Example test: AndroidVersionPluginIntgTest
	"""Querying the mapped value of flatmap\(provider\(task 'calculateBuildConfigBuildTime', class net\.twisterrob\.gradle\.android\.tasks\.CalculateBuildTimeTask\)\) before task '(:.+?)?:calculateBuildConfigBuildTime' has completed has been deprecated\. This will fail with an error in Gradle 7\.0\. Consult the upgrading guide for further information: https://docs\.gradle\.org/6\.7\.1/userguide/upgrading_version_6\.html#querying_a_mapped_output_property_of_a_task_before_the_task_has_completed"""
)
doNotNagAboutPattern( // Task path might be in sub-module.
	"6.7.1",
	"""^4\.0\.\d$""",
	// Example test: AndroidVersionPluginIntgTest
	"""Querying the mapped value of flatmap\(provider\(task 'calculateBuildConfigVCSRevisionInfo', class net\.twisterrob\.gradle\.android\.tasks\.CalculateVCSRevisionInfoTask\)\) before task '(:.+?)?:calculateBuildConfigVCSRevisionInfo' has completed has been deprecated\. This will fail with an error in Gradle 7\.0\. Consult the upgrading guide for further information: https://docs\.gradle\.org/6\.7\.1/userguide/upgrading_version_6\.html#querying_a_mapped_output_property_of_a_task_before_the_task_has_completed"""
)
doNotNagAbout(
	"7.6",
	"""^4\.2\.\d$""",
	// > Task :mergeDebugNativeLibs NO-SOURCE
	"Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property projectNativeLibs with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/7.6/userguide/upgrading_version_7.html#empty_directories_file_tree"
)
doNotNagAbout(
	"7.6",
	"""^(4\.2\.\d|7\.0\.\d)$""",
	// > Task :compileDebugRenderscript NO-SOURCE
	"Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property sourceDirs with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/7.6/userguide/upgrading_version_7.html#empty_directories_file_tree"
)
doNotNagAbout(
	"7.6",
	"""^(4\.2\.\d|7\.0\.\d)$""",
	// > Task :compileDebugAidl NO-SOURCE
	"Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property sourceFiles with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/7.6/userguide/upgrading_version_7.html#empty_directories_file_tree"
)
doNotNagAbout(
	"7.6",
	"""^(4\.2\.\d|7\.0\.\d)$""",
	// > Task :stripDebugDebugSymbols NO-SOURCE
	"Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property inputFiles with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/7.6/userguide/upgrading_version_7.html#empty_directories_file_tree"
)
doNotNagAbout(
	"7.6",
	"""^(4\.2\.\d|7\.0\.\d)$""",
	// > Task :bundleLibResDebug NO-SOURCE
	"Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property resources with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/7.6/userguide/upgrading_version_7.html#empty_directories_file_tree"
)
doNotNagAbout(
	"7.6",
	"""^(4\.2\.\d|7\.0\.\d|7\.1\.\d|7\.2\.\d)$""",
	// Ignore warning for https://issuetracker.google.com/issues/218478028 since Gradle 7.5, it's going to be fixed in AGP 7.3.
	// Example test: AndroidBuildPluginIntgTest.`adds custom resources and BuildConfig values`
	"IncrementalTaskInputs has been deprecated. This is scheduled to be removed in Gradle 8.0. On method 'IncrementalTask.taskAction\$gradle_core' use 'org.gradle.work.InputChanges' instead. Consult the upgrading guide for further information: https://docs.gradle.org/7.6/userguide/upgrading_version_7.html#incremental_task_inputs_deprecation"
)
