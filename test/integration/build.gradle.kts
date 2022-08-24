plugins {
	kotlin
	id("java-gradle-plugin")
}

dependencies {
	implementation(projects.plugin)
	implementation(projects.quality)
	implementation(projects.test)

	testImplementation(projects.test.internal)
	testImplementation(testFixtures(projects.plugin.base))
}
