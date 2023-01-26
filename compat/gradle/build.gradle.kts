plugins {
	id("net.twisterrob.gradle.build.module.library")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName.set("twister-compat-gradle")
description = "Gradle Compatibility: Support methods for compatibility with all supported Gradle versions."

dependencies {
	implementation(gradleApiWithoutKotlin())

	testImplementation(projects.test.internal)
}

tasks.named<Test>("test") {
	// TODEL Java 16 vs Gradle 7+ https://github.com/gradle/gradle/issues/18647
	// Example test: TaskExtensionsKtTest.`task provider was launched without qualification`
	jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
}
