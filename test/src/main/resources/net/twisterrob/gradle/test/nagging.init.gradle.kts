import net.twisterrob.gradle.nagging.doNotNagAboutForTest
import net.twisterrob.gradle.nagging.doNotNagAboutPatternForTest
import net.twisterrob.gradle.nagging.doNotNagAboutStackForTest
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
doNotNagAboutForTest(
	"7.6" to "8.0",
	"7.0" to "7.1",
	// > Task :compileDebugRenderscript NO-SOURCE
	"Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property sourceDirs with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#empty_directories_file_tree"
)
doNotNagAboutForTest(
	"7.6" to "8.0",
	"7.0" to "7.1",
	// > Task :compileDebugAidl NO-SOURCE
	"Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property sourceFiles with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#empty_directories_file_tree"
)
doNotNagAboutForTest(
	"7.6" to "8.0",
	"7.0" to "7.1",
	// > Task :stripDebugDebugSymbols NO-SOURCE
	"Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property inputFiles with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#empty_directories_file_tree"
)
doNotNagAboutForTest(
	"7.6" to "8.0",
	"7.0" to "7.1",
	// > Task :bundleLibResDebug NO-SOURCE
	"Relying on FileTrees for ignoring empty directories when using @SkipWhenEmpty has been deprecated. This is scheduled to be removed in Gradle 8.0. Annotate the property resources with @IgnoreEmptyDirectories or remove @SkipWhenEmpty. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#empty_directories_file_tree"
)
doNotNagAboutForTest(
	"7.6" to "8.0",
	"7.0" to "7.3",
	// Ignore warning for https://issuetracker.google.com/issues/218478028 since Gradle 7.5, it's going to be fixed in AGP 7.3.
	// Example test: AndroidBuildPluginIntgTest.`adds custom resources and BuildConfig values`
	"IncrementalTaskInputs has been deprecated. This is scheduled to be removed in Gradle 8.0. On method 'IncrementalTask.taskAction\$gradle_core' use 'org.gradle.work.InputChanges' instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#incremental_task_inputs_deprecation"
)
doNotNagAboutStackForTest(
	"8.0" to "8.3",
	"7.0" to "7.4.1",
	// Ignore warning for https://issuetracker.google.com/issues/264177800 since Gradle 8.0, it's going to be fixed in AGP 7.4.1.
	// This only shows up during CONFIGURATION phase, and only if a test task is needed (about 4 tests at the moment).
	// This means any build could trigger it, so putting it here into a global place to prevent false failures.
	"The Report.destination property has been deprecated. This is scheduled to be removed in Gradle 9.0. Please use the outputLocation property instead. See https://docs.gradle.org/${gradleVersion}/dsl/org.gradle.api.reporting.Report.html#org.gradle.api.reporting.Report:destination for more details.",
	"at com.android.build.gradle.tasks.factory.AndroidUnitTest\$CreationAction.configure"
)
doNotNagAboutStackForTest(
	"8.0" to "8.3",
	"8.1" to "8.2",
	// > Task :checkDebugUnitTestAarMetadata, :mergeDebugUnitTestResources, :processDebugUnitTestManifest, :mergeDebugUnitTestAssets
	// Ignore warning for https://issuetracker.google.com/issues/279306626, it'll be fixed when AGP's minimum is Gradle 8.2.
	"The BuildIdentifier.getName() method has been deprecated. This is scheduled to be removed in Gradle 9.0. Use getBuildPath() to get a unique identifier for the build. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#build_identifier_name_and_current_deprecation",
	"at com.android.build.gradle.internal.ide.dependencies.BuildMappingUtils.getIdString(BuildMapping.kt:48)"
)
doNotNagAboutStackForTest(
	"8.0" to "8.3",
	"8.1" to "8.2",
	// > Task :generateDebugLintModel. :lintAnalyzeDebug, :lintReportDebug
	// Ignore warning for https://issuetracker.google.com/issues/279306626, it'll be fixed when AGP's minimum is Gradle 8.2.
	"The BuildIdentifier.isCurrentBuild() method has been deprecated. This is scheduled to be removed in Gradle 9.0. Use getBuildPath() to get a unique identifier for the build. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#build_identifier_name_and_current_deprecation",
	"at com.android.build.gradle.internal.ide.dependencies.BuildMappingUtils.getBuildId(BuildMapping.kt:40)"
)

if ("@net.twisterrob.test.kotlin.pluginVersion@" < "1.6.0") {
	// https://youtrack.jetbrains.com/issue/KT-47867 was fixed in 1.6.0,
	// so disable these warnings for anything that uses Kotlin < 1.6.
	doNotNagAboutForTest(
		"7.6" to "8.0",
		"0.0" to "100.0",
		"IncrementalTaskInputs has been deprecated. This is scheduled to be removed in Gradle 8.0. On method 'KaptWithoutKotlincTask.compile' use 'org.gradle.work.InputChanges' instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#incremental_task_inputs_deprecation"
	)
	doNotNagAboutForTest(
		"7.6" to "8.0",
		"0.0" to "100.0",
		"IncrementalTaskInputs has been deprecated. This is scheduled to be removed in Gradle 8.0. On method 'AbstractKotlinCompile.execute' use 'org.gradle.work.InputChanges' instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#incremental_task_inputs_deprecation"
	)
}

if ("@net.twisterrob.test.kotlin.pluginVersion@" < "1.7.0") {
	// https://youtrack.jetbrains.com/issue/KT-32805#focus=Comments-27-5915479.0-0 was fixed in 1.7.0.
	// > Task :kaptGenerateStubsDebugKotlin
	// > Task :kaptGenerateStubsDebugUnitTestKotlin
	doNotNagAboutStackForTest(
		"8.0" to "8.3",
		"0.0" to "100.0",
		"The AbstractCompile.destinationDir property has been deprecated. This is scheduled to be removed in Gradle 9.0. Please use the destinationDirectory property instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#compile_task_wiring",
		"at org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask.isSourceRootAllowed"
	)
	doNotNagAboutStackForTest(
		"8.0" to "8.3",
		"0.0" to "100.0",
		"The AbstractCompile.destinationDir property has been deprecated. This is scheduled to be removed in Gradle 9.0. Please use the destinationDirectory property instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#compile_task_wiring",
		"at org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask.setupCompilerArgs"
	)
	doNotNagAboutStackForTest(
		"8.0" to "8.3",
		"0.0" to "100.0",
		"The AbstractCompile.destinationDir property has been deprecated. This is scheduled to be removed in Gradle 9.0. Please use the destinationDirectory property instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#compile_task_wiring",
		"at org.jetbrains.kotlin.compilerRunner.GradleCompilerRunner\$Companion.buildModulesInfo\$kotlin_gradle_plugin"
	)
	doNotNagAboutStackForTest(
		"8.0" to "8.3",
		"0.0" to "100.0",
		"The AbstractCompile.destinationDir property has been deprecated. This is scheduled to be removed in Gradle 9.0. Please use the destinationDirectory property instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#compile_task_wiring",
		"at org.jetbrains.kotlin.gradle.plugin.Android25ProjectHandler\$wireKotlinTasks\$preJavaKotlinOutput\$1.call"
	)
}

if ("@net.twisterrob.test.kotlin.pluginVersion@" < "1.7.20") {
	// https://youtrack.jetbrains.com/issue/KT-53882
	// https://youtrack.jetbrains.com/issue/KT-57908
	// > Configure project :
	doNotNagAboutStackForTest(
		"8.0" to "8.3",
		"0.0" to "100.0",
		"The org.gradle.util.WrapUtil type has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#org_gradle_util_reports_deprecations",
		// "at org.jetbrains.kotlin.gradle.plugin.mpp.AbstractKotlinTarget.<init>(kotlinTargets.kt:266)" // 1.6.21
		// "at org.jetbrains.kotlin.gradle.plugin.mpp.AbstractKotlinTarget.<init>(kotlinTargets.kt:269)" // 1.7.0, 1.7.10
		"at org.jetbrains.kotlin.gradle.plugin.mpp.AbstractKotlinTarget.<init>(kotlinTargets.kt:"
	)
}

if ("@net.twisterrob.test.kotlin.pluginVersion@" < "1.7.20") {
	// https://youtrack.jetbrains.com/issue/KT-47047
	// > Configure project :
	doNotNagAboutStackForTest(
		"8.0" to "8.3",
		"0.0" to "100.0",
		"The org.gradle.api.plugins.Convention type has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#deprecated_access_to_conventions",
		"at org.jetbrains.kotlin.gradle.plugin.GradleUtilsKt.getConvention(gradleUtils.kt:30)"
	)
	doNotNagAboutStackForTest(
		"8.0" to "8.3",
		"0.0" to "100.0",
		"The org.gradle.api.plugins.Convention type has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#deprecated_access_to_conventions",
		"at org.jetbrains.kotlin.gradle.plugin.GradleUtilsKt.getConvention(gradleUtils.kt:30)"
	)
}

if ("@net.twisterrob.test.kotlin.pluginVersion@" < "1.7.0") {
	// https://github.com/JetBrains/kotlin/commit/bc8f795f71ed6cbc05fc542c6a32f26da003d718
	doNotNagAboutStackForTest(
		"8.0" to "8.3",
		"0.0" to "100.0",
		"The Project.getConvention() method has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#deprecated_access_to_conventions",
		"at org.jetbrains.kotlin.gradle.plugin.mpp.KotlinCompilationsKt.ownModuleName(kotlinCompilations.kt:373)"
	)
	doNotNagAboutStackForTest(
		"8.0" to "8.3",
		"0.0" to "100.0",
		"The org.gradle.api.plugins.Convention type has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#deprecated_access_to_conventions",
		"at org.jetbrains.kotlin.gradle.plugin.mpp.KotlinCompilationsKt.ownModuleName(kotlinCompilations.kt:373)"
	)
	doNotNagAboutStackForTest(
		"8.0" to "8.3",
		"0.0" to "100.0",
		"The org.gradle.api.plugins.BasePluginConvention type has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#base_convention_deprecation",
		"at org.jetbrains.kotlin.gradle.plugin.mpp.KotlinCompilationsKt.ownModuleName(kotlinCompilations.kt:373)"
	)
}

if ("@net.twisterrob.test.kotlin.pluginVersion@" < "1.9.0") {
	// https://youtrack.jetbrains.com/issue/KT-52976
	//"at org.jetbrains.kotlin.gradle.plugin.GradleUtilsKt.addConvention(gradleUtils.kt:37)
}

if ("@net.twisterrob.test.kotlin.pluginVersion@" < "1.7.20") {
	// https://youtrack.jetbrains.com/issue/KT-47047
	// https://github.com/JetBrains/kotlin/commit/c495c07b1ae8df3ebc683ba925cecf26daaf9c1e
	doNotNagAboutPatternForTest(
		"8.0" to "8.3",
		"0.0" to "100.0",
		""
				+ "("
				+ Regex.escape("The Project.getConvention() method has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#deprecated_access_to_conventions")
				+ "|"
				+ Regex.escape("The org.gradle.api.plugins.Convention type has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#deprecated_access_to_conventions")
				+ "|"
				+ Regex.escape("The org.gradle.api.plugins.JavaPluginConvention type has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#java_convention_deprecation")
				+ ")"
				+ ".*?"
				+ "("
				+ Regex.escape("at org.jetbrains.kotlin.gradle.plugin.AbstractKotlinPlugin\$Companion.setUpJavaSourceSets\$kotlin_gradle_plugin(KotlinPlugin.kt:")
				+ "|"
				+ Regex.escape("at org.jetbrains.kotlin.gradle.scripting.internal.ScriptingGradleSubplugin\$apply\$1.execute(ScriptingGradleSubplugin.kt:")
				+ ")"
				+ ".*"
	)
}
