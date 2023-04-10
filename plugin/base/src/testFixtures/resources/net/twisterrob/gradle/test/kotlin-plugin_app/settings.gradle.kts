val gradleVersion = gradle.gradleVersion
@Suppress("UNCHECKED_CAST")
val doNotNagAbout = settings.extra["doNotNagAbout"] as (String, String, String) -> Unit
@Suppress("UNCHECKED_CAST")
val doNotNagAboutStack = settings.extra["doNotNagAboutStack"] as (String, String, String, String) -> Unit

if ("@net.twisterrob.test.kotlin.pluginVersion@" < "1.6.0") {
	// https://youtrack.jetbrains.com/issue/KT-47867 was fixed in 1.6.0,
	// so disable these warnings for anything that uses Kotlin < 1.6.
	doNotNagAbout(
			"7.6.1",
			"""^.*$""",
			"IncrementalTaskInputs has been deprecated. This is scheduled to be removed in Gradle 8.0. On method 'KaptWithoutKotlincTask.compile' use 'org.gradle.work.InputChanges' instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#incremental_task_inputs_deprecation"
	)
	doNotNagAbout(
			"7.6.1",
			"""^.*$""",
			"IncrementalTaskInputs has been deprecated. This is scheduled to be removed in Gradle 8.0. On method 'AbstractKotlinCompile.execute' use 'org.gradle.work.InputChanges' instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#incremental_task_inputs_deprecation"
	)
}

if ("@net.twisterrob.test.kotlin.pluginVersion@" < "1.7.0") {
	// https://youtrack.jetbrains.com/issue/KT-32805#focus=Comments-27-5915479.0-0 was fixed in 1.7.0.
	// > Task :kaptGenerateStubsDebugKotlin
	// > Task :kaptGenerateStubsDebugUnitTestKotlin
	doNotNagAboutStack(
			"8.1",
			"""^.*$""",
			"The AbstractCompile.destinationDir property has been deprecated. This is scheduled to be removed in Gradle 9.0. Please use the destinationDirectory property instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#compile_task_wiring",
			"at org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask.isSourceRootAllowed"
	)
	doNotNagAboutStack(
			"8.1",
			"""^.*$""",
			"The AbstractCompile.destinationDir property has been deprecated. This is scheduled to be removed in Gradle 9.0. Please use the destinationDirectory property instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#compile_task_wiring",
			"at org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask.setupCompilerArgs"
	)
	doNotNagAboutStack(
			"8.1",
			"""^.*$""",
			"The AbstractCompile.destinationDir property has been deprecated. This is scheduled to be removed in Gradle 9.0. Please use the destinationDirectory property instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#compile_task_wiring",
			"at org.jetbrains.kotlin.compilerRunner.GradleCompilerRunner\$Companion.buildModulesInfo\$kotlin_gradle_plugin"
	)
	doNotNagAboutStack(
			"8.1",
			"""^.*$""",
			"The AbstractCompile.destinationDir property has been deprecated. This is scheduled to be removed in Gradle 9.0. Please use the destinationDirectory property instead. Consult the upgrading guide for further information: https://docs.gradle.org/${gradleVersion}/userguide/upgrading_version_7.html#compile_task_wiring",
			"at org.jetbrains.kotlin.gradle.plugin.Android25ProjectHandler\$wireKotlinTasks\$preJavaKotlinOutput\$1.call"
	)
}
