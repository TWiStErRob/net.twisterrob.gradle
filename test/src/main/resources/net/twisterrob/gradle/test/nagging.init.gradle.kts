import net.twisterrob.gradle.nagging.doNotNagAbout
import net.twisterrob.gradle.nagging.doNotNagAboutPattern
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
	"7.6.1",
	"""^7\.0\.\d$""",
	// > Task :compileDebugRenderscript NO-SOURCE
	"Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property sourceDirs with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#empty_directories_file_tree"
)
doNotNagAbout(
	"7.6.1",
	"""^7\.0\.\d$""",
	// > Task :compileDebugAidl NO-SOURCE
	"Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property sourceFiles with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#empty_directories_file_tree"
)
doNotNagAbout(
	"7.6.1",
	"""^7\.0\.\d$""",
	// > Task :stripDebugDebugSymbols NO-SOURCE
	"Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property inputFiles with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#empty_directories_file_tree"
)
doNotNagAbout(
	"7.6.1",
	"""^7\.0\.\d$""",
	// > Task :bundleLibResDebug NO-SOURCE
	"Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property resources with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#empty_directories_file_tree"
)
doNotNagAbout(
	"7.6.1",
	"""^(7\.0\.\d|7\.1\.\d|7\.2\.\d)$""",
	// Ignore warning for https://issuetracker.google.com/issues/218478028 since Gradle 7.5, it's going to be fixed in AGP 7.3.
	// Example test: AndroidBuildPluginIntgTest.`adds custom resources and BuildConfig values`
	"IncrementalTaskInputs has been deprecated. This is scheduled to be removed in Gradle 8.0. On method 'IncrementalTask.taskAction\$gradle_core' use 'org.gradle.work.InputChanges' instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#incremental_task_inputs_deprecation"
)
doNotNagAbout(
	"8.1.1",
	"""^(7\.0\.\d|7\.1\.\d|7\.2\.\d|7\.3\.\d|7\.4\.0)$""",
	// Ignore warning for https://issuetracker.google.com/issues/264177800 since Gradle 8.0, it's going to be fixed in AGP 7.4.1.
	// This only shows up during CONFIGURATION phase, and only if a test task is needed (about 4 tests at the moment).
	// This means any build could trigger it, so putting it here into a global place to prevent false failures.
	"The Report.destination property has been deprecated. This is scheduled to be removed in Gradle 9.0. Please use the outputLocation property instead. See https://docs.gradle.org/${gradleVersion}/dsl/org.gradle.api.reporting.Report.html#org.gradle.api.reporting.Report:destination for more details.",
	"at com.android.build.gradle.tasks.factory.AndroidUnitTest\$CreationAction.configure"
)
