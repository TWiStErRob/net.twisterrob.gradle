plugins {
	id("net.twisterrob.gradle.build.module.library")
}

dependencies {
	api(enforcedPlatform(libs.kotlin.bom)) {
		version { require(libs.versions.kotlin.build.get()) }
	}

	api(projects.test)

	api(gradleApi())
	api(gradleTestKit())

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
}
