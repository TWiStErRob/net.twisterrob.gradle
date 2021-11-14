plugins {
	`java-library`
}

dependencies {
	api(projects.test)

	api(gradleApiWithoutKotlin())
	api(gradleTestKitWithoutKotlin())

	api(Libs.Annotations.jetbrains)

	api(Libs.JUnit4.junit4) // needed for GradleRunnerRule superclass even when using Extension
	api(Libs.JUnit5.`junit.api`)
	api(Libs.JUnit5.`junit.params`)
	runtimeOnly(Libs.JUnit5.`junit.engine`)

	api(Libs.Hamcrest.hamcrest)

	api(Libs.Mockito.`mockito.core`)
	api(Libs.Mockito.`mockito.junit5`)
	api(Libs.Mockito.`mockito.kotlin`)

	api(Libs.mockk)

	api(Libs.JFixture.`jfixture.java`)

	// TODO use buildSrc sourceOnly configuration
	// only here so IDEA can browse the source files of this dependency when getting a stack trace or finding usages
	testRuntimeOnly(Libs.Android.lint) { isTransitive = false }
	testRuntimeOnly(Libs.Android.lintApi) { isTransitive = false }
	testRuntimeOnly(Libs.Android.lintGradle) { isTransitive = false }
	testRuntimeOnly(Libs.Android.lintGradleApi) { isTransitive = false }
	testRuntimeOnly(Libs.Android.lintChecks) { isTransitive = false }
}
