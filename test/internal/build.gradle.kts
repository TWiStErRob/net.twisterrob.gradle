plugins {
	`java-library`
}

dependencies {
	api(projects.test)

	api(gradleApiWithoutKotlin())
	api(gradleTestKitWithoutKotlin())

	api(libs.jetbrains)

	api(libs.junit4) // needed for GradleRunnerRule superclass even when using Extension
	api(libs.junit.api)
	api(libs.junit.params)
	runtimeOnly(libs.junit.engine)

	api(libs.hamcrest)

	api(libs.mockito.core)
	api(libs.mockito.junit5)
	api(libs.mockito.kotlin)

	api(libs.mockk)

	api(libs.jfixture.java)

	// TODO use buildSrc sourceOnly configuration
	// only here so IDEA can browse the source files of this dependency when getting a stack trace or finding usages
	testRuntimeOnly(libs.lint) { isTransitive = false }
	testRuntimeOnly(libs.lintApi) { isTransitive = false }
	testRuntimeOnly(libs.lintGradle) { isTransitive = false }
	testRuntimeOnly(libs.lintGradleApi) { isTransitive = false }
	testRuntimeOnly(libs.lintChecks) { isTransitive = false }
}
