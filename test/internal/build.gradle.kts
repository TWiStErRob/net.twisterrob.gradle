plugins {
	id("org.gradle.java-library")
	id("org.jetbrains.kotlin.jvm")
	id("net.twisterrob.gradle.build.detekt")
}

dependencies {
	api(projects.test)

	api(gradleApiWithoutKotlin())
	api(gradleTestKitWithoutKotlin())

	api(libs.annotations.jetbrains)

	api(libs.junit.pioneer)
	api(libs.junit.legacy) // needed for GradleRunnerRule superclass even when using Extension
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
	testRuntimeOnly(libs.android.lint.main) { isTransitive = false }
	testRuntimeOnly(libs.android.lint.api) { isTransitive = false }
	testRuntimeOnly(libs.android.lint.gradle) { isTransitive = false }
	testRuntimeOnly(libs.android.lint.checks) { isTransitive = false }
}
