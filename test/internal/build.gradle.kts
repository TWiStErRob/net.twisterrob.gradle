plugins {
	id("net.twisterrob.gradle.build.module.library")
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
}

tasks.named<Test>("test") {
	if (javaVersion.isJava9Compatible) {
		// TODEL Java 16 vs AssertionFailedError https://github.com/ota4j-team/opentest4j/issues/70
		// Example test: WithRootCauseKtTest.`registering a task preConfigures, but does not create it`
		jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
	}
}
