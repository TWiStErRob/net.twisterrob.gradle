plugins {
	id("net.twisterrob.gradle.build.module.gradle-plugin")
	id("net.twisterrob.gradle.build.publish")
}

base.archivesName.set("twister-gradle-test")
description = "Test: Gradle test plugin."

gradlePlugin {
	@Suppress("UnstableApiUsage", "StringLiteralDuplication")
	plugins {
		create("gradleTest") {
			id = "net.twisterrob.gradle.plugin.gradle.test"
			displayName = "Gradle Testing Plugin"
			description = """
				TWiStErRob's testing helper plugin and testing utilities for Gradle based integration tests.
				
				A wrapper for `org.gradle.testkit.runner.GradleRunner` to reduce boilerplate.
				Also contains helpers for building a project on disk from code and assert the results of an execution.
			""".trimIndent()
			tags.set(setOf("gradle", "test", "plugin-development"))
			implementationClass = "net.twisterrob.gradle.test.TestPlugin"
			deprecateId(project, "net.twisterrob.gradle.test")
		}
	}
}

dependencies {
	compileOnly(gradleTestKitWithoutKotlin())

	compileOnly(libs.junit.legacy)
	compileOnly(libs.junit.api)
	compileOnly(libs.mockito.kotlin)
	compileOnly(libs.annotations.jsr305)
	compileOnly(libs.annotations.jetbrains)

	api(projects.common)

	testImplementation(gradleApiWithoutKotlin())
	testImplementation(gradleTestKitWithoutKotlin())
	testImplementation(projects.test.internal)
}

exposeTestResources()

// Need to depend on the real artifact so TestPluginTest can work
tasks.named<Test>("test") {
	val jarOutput = tasks.jar.get().outputs.files
	inputs.files(jarOutput).withPathSensitivity(PathSensitivity.RELATIVE)
	doFirst {
		val jarArtifactPath = jarOutput.singleFile.parentFile
		(this as Test).jvmArgs("-Dnet.twisterrob.gradle.test.artifactPath=${jarArtifactPath}")
	}
}

tasks.named<PluginUnderTestMetadata>("pluginUnderTestMetadata") {
	val jarOutput = tasks.jar.get().outputs.files
	inputs.files(jarOutput).withPathSensitivity(PathSensitivity.RELATIVE)
	pluginClasspath.apply {
		setFrom()
		from(configurations.runtimeClasspath - configurations.compileClasspath)
		from(jarOutput.singleFile)
	}
}

operator fun Provider<Configuration>.minus(other: Provider<Configuration>): Provider<FileCollection> =
	this.flatMap { one -> other.map { two -> one - two } }
