val gradleVersion = gradle.gradleVersion
@Suppress("UNCHECKED_CAST")
val doNotNagAboutForTest = settings.extra["doNotNagAboutForTest"] as (String, String, String) -> Unit
@Suppress("UNCHECKED_CAST")
val doNotNagAboutStackForTest = settings.extra["doNotNagAboutStackForTest"] as (String, String, String, String) -> Unit

if ("@net.twisterrob.test.kotlin.pluginVersion@" < "1.6.0") {
	// https://youtrack.jetbrains.com/issue/KT-47867 was fixed in 1.6.0,
	// so disable these warnings for anything that uses Kotlin < 1.6.
	doNotNagAboutForTest(
		"7.6.1",
		"""^.*$""",
		"IncrementalTaskInputs has been deprecated. This is scheduled to be removed in Gradle 8.0. On method 'KaptWithoutKotlincTask.compile' use 'org.gradle.work.InputChanges' instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#incremental_task_inputs_deprecation"
	)
	doNotNagAboutForTest(
		"7.6.1",
		"""^.*$""",
		"IncrementalTaskInputs has been deprecated. This is scheduled to be removed in Gradle 8.0. On method 'AbstractKotlinCompile.execute' use 'org.gradle.work.InputChanges' instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#incremental_task_inputs_deprecation"
	)
}

if ("@net.twisterrob.test.kotlin.pluginVersion@" < "1.7.0") {
	// https://youtrack.jetbrains.com/issue/KT-32805#focus=Comments-27-5915479.0-0 was fixed in 1.7.0.
	// > Task :kaptGenerateStubsDebugKotlin
	// > Task :kaptGenerateStubsDebugUnitTestKotlin
	doNotNagAboutStackForTest(
		"8.2",
		"""^.*$""",
		"The AbstractCompile.destinationDir property has been deprecated. This is scheduled to be removed in Gradle 9.0. Please use the destinationDirectory property instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#compile_task_wiring",
		"at org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask.isSourceRootAllowed"
	)
	doNotNagAboutStackForTest(
		"8.2",
		"""^.*$""",
		"The AbstractCompile.destinationDir property has been deprecated. This is scheduled to be removed in Gradle 9.0. Please use the destinationDirectory property instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#compile_task_wiring",
		"at org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask.setupCompilerArgs"
	)
	doNotNagAboutStackForTest(
		"8.2",
		"""^.*$""",
		"The AbstractCompile.destinationDir property has been deprecated. This is scheduled to be removed in Gradle 9.0. Please use the destinationDirectory property instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#compile_task_wiring",
		"at org.jetbrains.kotlin.compilerRunner.GradleCompilerRunner\$Companion.buildModulesInfo\$kotlin_gradle_plugin"
	)
	doNotNagAboutStackForTest(
		"8.2",
		"""^.*$""",
		"The AbstractCompile.destinationDir property has been deprecated. This is scheduled to be removed in Gradle 9.0. Please use the destinationDirectory property instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#compile_task_wiring",
		"at org.jetbrains.kotlin.gradle.plugin.Android25ProjectHandler\$wireKotlinTasks\$preJavaKotlinOutput\$1.call"
	)
}

if ("@net.twisterrob.test.kotlin.pluginVersion@" < "1.7.20") {
	// https://youtrack.jetbrains.com/issue/KT-53882
	// https://youtrack.jetbrains.com/issue/KT-57908
	// > Configure project :
	doNotNagAboutStackForTest(
		"8.2",
		"""^.*$""",
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
		"8.2",
		"""^.*$""",
		"The org.gradle.api.plugins.Convention type has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#deprecated_access_to_conventions",
		"at org.jetbrains.kotlin.gradle.plugin.GradleUtilsKt.getConvention(gradleUtils.kt:30)"
	)
	doNotNagAboutStackForTest(
		"8.2",
		"""^.*$""",
		"The org.gradle.api.plugins.Convention type has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#deprecated_access_to_conventions",
		"at org.jetbrains.kotlin.gradle.plugin.GradleUtilsKt.getConvention(gradleUtils.kt:30)"
	)
}

if ("@net.twisterrob.test.kotlin.pluginVersion@" < "1.7.0") {
	// https://github.com/JetBrains/kotlin/commit/bc8f795f71ed6cbc05fc542c6a32f26da003d718
	doNotNagAboutStackForTest(
		"8.2",
		"""^.*$""",
		"The Project.getConvention() method has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#deprecated_access_to_conventions",
		"at org.jetbrains.kotlin.gradle.plugin.mpp.KotlinCompilationsKt.ownModuleName(kotlinCompilations.kt:373)"
	)
	doNotNagAboutStackForTest(
		"8.2",
		"""^.*$""",
		"The org.gradle.api.plugins.Convention type has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#deprecated_access_to_conventions",
		"at org.jetbrains.kotlin.gradle.plugin.mpp.KotlinCompilationsKt.ownModuleName(kotlinCompilations.kt:373)"
	)
	doNotNagAboutStackForTest(
		"8.2",
		"""^.*$""",
		"The org.gradle.api.plugins.BasePluginConvention type has been deprecated. This is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_8.html#base_convention_deprecation",
		"at org.jetbrains.kotlin.gradle.plugin.mpp.KotlinCompilationsKt.ownModuleName(kotlinCompilations.kt:373)"
	)
}
