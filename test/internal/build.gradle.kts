plugins {
	`java-library`
}

dependencies {
	api(projects.test)

	api(gradleApiWithoutKotlin())
	api(gradleTestKitWithoutKotlin())

	api(Libs.Annotations.jetbrains)

	api(Libs.JUnit4.library) // needed for GradleRunnerRule superclass even when using Extension
	api(Libs.JUnit5.api)
	api(Libs.JUnit5.params)
	runtimeOnly(Libs.JUnit5.engine)

	api(Libs.Hamcrest.best)

	api(Libs.Mockito.core)
	api(Libs.Mockito.junit5)
	api(Libs.Mockito.kotlin)

	api(Libs.mockk)

	api(Libs.JFixture.java)

	// TODO use buildSrc sourceOnly configuration
	// only here so IDEA can browse the source files of this dependency when getting a stack trace or finding usages
	testRuntimeOnly(Libs.Android.lint) { isTransitive = false }
	testRuntimeOnly(Libs.Android.lintApi) { isTransitive = false }
	testRuntimeOnly(Libs.Android.lintGradle) { isTransitive = false }
	testRuntimeOnly(Libs.Android.lintGradleApi) { isTransitive = false }
	testRuntimeOnly(Libs.Android.lintChecks) { isTransitive = false }
}
