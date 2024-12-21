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

apply<net.twisterrob.gradle.nagging.NaggingPluginForTest>()

//System.setProperty("net.twisterrob.gradle.nagging.diagnostics", "true")

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
	"8.0" to "8.9",
	"7.4" to "8.2",
	// > Task :checkDebugUnitTestAarMetadata, :mergeDebugUnitTestResources, :processDebugUnitTestManifest, :mergeDebugUnitTestAssets
	// Example test: AndroidBuildPluginIntgTest.`can disable buildConfig decoration (debug)`
	// Ignore warning for https://issuetracker.google.com/issues/279306626, it'll be fixed when AGP's minimum is Gradle 8.2 (8.2.0-alpha13).
	"The BuildIdentifier.getName() method has been deprecated. This is scheduled to be removed in Gradle 9.0. Use getBuildPath() to get a unique identifier for the build. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#build_identifier_name_and_current_deprecation",
	"at com.android.build.gradle.internal.ide.dependencies.BuildMappingUtils.getIdString(BuildMapping.kt:48)"
)
doNotNagAboutStackForTest(
	"8.0" to "8.9",
	"7.4" to "8.2",
	// > Task :generateDebugLintModel. :lintAnalyzeDebug, :lintReportDebug
	// > Task :lintAnalyzeDebug
	// Example test: HtmlReportTaskTest.`task is re-executed when lint results are changed`
	// Ignore warning for https://issuetracker.google.com/issues/279306626, it'll be fixed when AGP's minimum is Gradle 8.2 (8.2.0-alpha13).
	"The BuildIdentifier.isCurrentBuild() method has been deprecated. This is scheduled to be removed in Gradle 9.0. Use getBuildPath() to get a unique identifier for the build. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#build_identifier_name_and_current_deprecation",
	"at com.android.build.gradle.internal.ide.dependencies.BuildMappingUtils.getBuildId(BuildMapping.kt:40)"
)
doNotNagAboutStackForTest(
	"8.0" to "8.9",
	"7.4" to "8.0",
	// > Task :lintAnalyzeDebug
	// Example test: HtmlReportTaskTest.`task is re-executed when lint results are changed`
	"The BuildIdentifier.isCurrentBuild() method has been deprecated. This is scheduled to be removed in Gradle 9.0. Use getBuildPath() to get a unique identifier for the build. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#build_identifier_name_and_current_deprecation",
	"at com.android.build.gradle.internal.dependency.ConstraintHandler\$alignWith\$1\$1.execute(ConstraintHandler.kt:68)"
)
doNotNagAboutStackForTest(
	"8.2" to "8.9",
	"7.4" to "8.0",
	// > Configure project : when using android.testOptions.unitTests.all { }
	// Example test: AndroidBuildPluginIntgTest.`can disable buildConfig decoration (debug)`
	"The org.gradle.util.ConfigureUtil type has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#org_gradle_util_reports_deprecations",
	"at com.android.build.gradle.internal.dsl.TestOptions\$UnitTestOptions\$all\$1.execute(TestOptions.kt:115)"
)
doNotNagAboutStackForTest(
	"8.3" to "8.9",
	"7.4" to "8.0",
	// > Configure project :
	// Example test: TestReportGeneratorIntgTest
	"The org.gradle.util.GUtil type has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#org_gradle_util_reports_deprecations",
	"at com.android.build.gradle.internal.api.DefaultAndroidSourceSet.<init>(DefaultAndroidSourceSet.kt:68)"
)
doNotNagAboutStackForTest(
	"8.7" to "8.9",
	"7.4" to "8.2",
	// > Configure project : in all Android tests
	// Example test: AndroidBuildPluginIntgTest.`can override compileSdk (debug)`
	"Declaring client module dependencies has been deprecated. This is scheduled to be removed in Gradle 9.0. Please use component metadata rules instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#declaring_client_module_dependencies",
	// AGP 7.4: line 138; AGP 8.0, 8.1: line 136
	"at com.android.build.gradle.internal.res.Aapt2FromMaven\$Companion.create(Aapt2FromMaven.kt:13"
)
doNotNagAboutPatternForTest(
	"8.8" to "8.9",
	"7.4" to "8.3",
	// > Task :generateDebugRFile
	// > Task :generateReleaseRFile
	// Example test: AndroidBuildPluginIntgTest.`can disable buildConfig decoration (debug)`
	// Example test: KotlinPluginIntgTest.`can test kotlin with JUnit in Android Test App`
	"Mutating the dependencies of configuration '(:.+)*:(release|debug)CompileClasspath'" +
			Regex.escape(" after it has been resolved or consumed. This behavior has been deprecated. This will fail with an error in Gradle 9.0. After a Configuration has been resolved, consumed as a variant, or used for generating published metadata, it should not be modified. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#mutate_configuration_after_locking") + ".*",
	//"at com.android.build.gradle.internal.dependency.ConstraintHandler\$alignWith\$1\$1.execute(ConstraintHandler.kt:56)"
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
	// Example test: KotlinPluginIntgTest.`can test kotlin with JUnit in Android Test App`
	doNotNagAboutStackForTest(
		"8.0" to "8.9",
		"0.0" to "100.0",
		"The AbstractCompile.destinationDir property has been deprecated. This is scheduled to be removed in Gradle 9.0. Please use the destinationDirectory property instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#compile_task_wiring",
		"at org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask.isSourceRootAllowed"
	)
	doNotNagAboutStackForTest(
		"8.0" to "8.9",
		"0.0" to "100.0",
		"The AbstractCompile.destinationDir property has been deprecated. This is scheduled to be removed in Gradle 9.0. Please use the destinationDirectory property instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#compile_task_wiring",
		"at org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask.setupCompilerArgs"
	)
	doNotNagAboutStackForTest(
		"8.0" to "8.9",
		"0.0" to "100.0",
		"The AbstractCompile.destinationDir property has been deprecated. This is scheduled to be removed in Gradle 9.0. Please use the destinationDirectory property instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#compile_task_wiring",
		"at org.jetbrains.kotlin.compilerRunner.GradleCompilerRunner\$Companion.buildModulesInfo\$kotlin_gradle_plugin"
	)
	doNotNagAboutStackForTest(
		"8.0" to "8.9",
		"0.0" to "100.0",
		"The AbstractCompile.destinationDir property has been deprecated. This is scheduled to be removed in Gradle 9.0. Please use the destinationDirectory property instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#compile_task_wiring",
		"at org.jetbrains.kotlin.gradle.plugin.Android25ProjectHandler\$wireKotlinTasks\$preJavaKotlinOutput\$1.call"
	)
	doNotNagAboutStackForTest(
		"8.9" to "8.10",
		"0.0" to "100.0",
		"The AbstractCompile.destinationDir property has been deprecated. This is scheduled to be removed in Gradle 9.0. Property was automatically upgraded to the lazy version. Please use the destinationDirectory property instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#compile_task_wiring",
		"at org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask.isSourceRootAllowed"
	)
	doNotNagAboutStackForTest(
		"8.9" to "8.10",
		"0.0" to "100.0",
		"The AbstractCompile.destinationDir property has been deprecated. This is scheduled to be removed in Gradle 9.0. Property was automatically upgraded to the lazy version. Please use the destinationDirectory property instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#compile_task_wiring",
		"at org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask.setupCompilerArgs"
	)
	doNotNagAboutStackForTest(
		"8.9" to "8.10",
		"0.0" to "100.0",
		"The AbstractCompile.destinationDir property has been deprecated. This is scheduled to be removed in Gradle 9.0. Property was automatically upgraded to the lazy version. Please use the destinationDirectory property instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#compile_task_wiring",
		"at org.jetbrains.kotlin.compilerRunner.GradleCompilerRunner\$Companion.buildModulesInfo\$kotlin_gradle_plugin"
	)
	doNotNagAboutStackForTest(
		"8.9" to "8.10",
		"0.0" to "100.0",
		"The AbstractCompile.destinationDir property has been deprecated. This is scheduled to be removed in Gradle 9.0. Property was automatically upgraded to the lazy version. Please use the destinationDirectory property instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#compile_task_wiring",
		"at org.jetbrains.kotlin.gradle.plugin.Android25ProjectHandler\$wireKotlinTasks\$preJavaKotlinOutput\$1.call"
	)
}

if ("@net.twisterrob.test.kotlin.pluginVersion@" < "1.7.20") {
	// https://youtrack.jetbrains.com/issue/KT-53882
	// https://youtrack.jetbrains.com/issue/KT-57908
	// > Configure project :
	// Example test: KotlinPluginIntgTest.`can test kotlin with JUnit in Android Test App`
	doNotNagAboutStackForTest(
		"8.0" to "8.10",
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
		"8.0" to "8.10",
		"0.0" to "100.0",
		"The org.gradle.api.plugins.Convention type has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#deprecated_access_to_conventions",
		"at org.jetbrains.kotlin.gradle.plugin.GradleUtilsKt.getConvention(gradleUtils.kt:30)"
	)
	doNotNagAboutStackForTest(
		"8.0" to "8.10",
		"0.0" to "100.0",
		"The org.gradle.api.plugins.Convention type has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#deprecated_access_to_conventions",
		"at org.jetbrains.kotlin.gradle.plugin.GradleUtilsKt.getConvention(gradleUtils.kt:30)"
	)
	// Example test: KotlinPluginIntgTest.`can test kotlin with JUnit in Android Test App`
	doNotNagAboutStackForTest(
		"8.7" to "8.10",
		"0.0" to "100.0",
		"The org.gradle.api.plugins.Convention type has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#deprecated_access_to_conventions",
		"at org.jetbrains.kotlin.gradle.plugin.mpp.SyncKotlinAndAndroidSourceSetsKt.setKotlinSourceSet(syncKotlinAndAndroidSourceSets.kt:145)"
	)
}

if ("@net.twisterrob.test.kotlin.pluginVersion@" < "1.7.0") {
	// https://github.com/JetBrains/kotlin/commit/bc8f795f71ed6cbc05fc542c6a32f26da003d718
	// > Task :test:kaptGenerateStubsDebugKotlin
	// Example test: KotlinPluginIntgTest.`can test kotlin with JUnit in Android Test App`
	doNotNagAboutStackForTest(
		"8.0" to "8.10",
		"0.0" to "100.0",
		"The Project.getConvention() method has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#deprecated_access_to_conventions",
		"at org.jetbrains.kotlin.gradle.plugin.mpp.KotlinCompilationsKt.ownModuleName(kotlinCompilations.kt:373)"
	)
	doNotNagAboutStackForTest(
		"8.0" to "8.10",
		"0.0" to "100.0",
		"The org.gradle.api.plugins.Convention type has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#deprecated_access_to_conventions",
		"at org.jetbrains.kotlin.gradle.plugin.mpp.KotlinCompilationsKt.ownModuleName(kotlinCompilations.kt:373)"
	)
	doNotNagAboutStackForTest(
		"8.0" to "8.10",
		"0.0" to "100.0",
		"The org.gradle.api.plugins.BasePluginConvention type has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#base_convention_deprecation",
		"at org.jetbrains.kotlin.gradle.plugin.mpp.KotlinCompilationsKt.ownModuleName(kotlinCompilations.kt:373)"
	)
	doNotNagAboutStackForTest(
		"8.5" to "8.10",
		"0.0" to "100.0",
		"The BasePluginExtension.archivesBaseName property has been deprecated. This is scheduled to be removed in Gradle 9.0. Please use the archivesName property instead. For more information, please refer to https://docs.gradle.org/${gradleVersion}/dsl/org.gradle.api.plugins.BasePluginExtension.html#org.gradle.api.plugins.BasePluginExtension:archivesName in the Gradle documentation.",
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
	// Example test: KotlinPluginIntgTest.`can compile Kotlin`
	doNotNagAboutPatternForTest(
		"8.0" to "8.10",
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
	// Example test: KotlinPluginIntgTest.`can compile Kotlin`
	doNotNagAboutStackForTest(
		"8.7" to "8.10",
		"0.0" to "100.0",
		"The org.gradle.api.plugins.Convention type has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#deprecated_access_to_conventions",
		"at org.jetbrains.kotlin.gradle.plugin.AbstractKotlinPlugin\$Companion\$setUpJavaSourceSets\$1.execute(KotlinPlugin.kt:1190)"
	)
}

if ("@net.twisterrob.test.kotlin.pluginVersion@" in "1.5.20".."2.0.10") {
	// https://youtrack.jetbrains.com/issue/KT-67838
	// > Configure project :
	// Example test: AndroidBuildPluginIntgTest.`can disable buildConfig decoration (debug)`
	// Introduced in 1.5.20: https://github.com/JetBrains/kotlin/commit/34e0a3caa890246946ec5fc0153a0b3dc4271244
	doNotNagAboutStackForTest(
		"8.8" to "8.10",
		"0.0" to "100.0",
		"The Configuration.fileCollection(Spec) method has been deprecated. This is scheduled to be removed in Gradle 9.0. Use Configuration.getIncoming().artifactView(Action) with a componentFilter instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#deprecate_filtered_configuration_file_and_filecollection_methods",
		when ("@net.twisterrob.test.kotlin.pluginVersion@") {
			in "1.6.20".."1.6.21" ->
				"at org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin\$createKaptKotlinTask\$kaptTaskProvider\$1.invoke(Kapt3KotlinGradleSubplugin.kt:569)"
			in "1.7.0".."1.7.10" ->
				"at org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin\$createKaptKotlinTask\$kaptTaskProvider\$1.invoke(Kapt3KotlinGradleSubplugin.kt:441)"
			in "1.7.20".."1.9.25" ->
				"at org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin\$createKaptKotlinTask\$2.invoke(Kapt3KotlinGradleSubplugin.kt:422)"
			in "2.0.0".."2.0.10" ->
				"at org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin\$createKaptKotlinTask\$2.invoke(Kapt3KotlinGradleSubplugin.kt:405)"
			else -> error("Don't know the line number yet for @net.twisterrob.test.kotlin.pluginVersion@.")
		}
	)
}
