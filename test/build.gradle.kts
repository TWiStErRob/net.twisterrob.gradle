plugins {
//	kotlin("jvm")
	`java-gradle-plugin`
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-gradle-test")
description = "Test: Gradle test plugin."

gradlePlugin {
	plugins {
		create("net.twisterrob.gradle.test") {
			id = "net.twisterrob.gradle.test"
			implementationClass = "net.twisterrob.gradle.test.TestPlugin"
		}
	}
}

dependencies {
	compileOnly(gradleTestKitWithoutKotlin())

	compileOnly(libs.junit.legacy)
	compileOnly(libs.junit.api)
	compileOnly(libs.annotations.jsr305)
	compileOnly(libs.annotations.jetbrains)

	testImplementation(gradleApiWithoutKotlin())
	testImplementation(gradleTestKitWithoutKotlin())
	testImplementation(projects.test.internal)
}

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

inline val TaskContainer.jar: TaskProvider<Jar>
	get() = named<Jar>("jar")

// Polyfill for Gradle 5
operator fun Provider<Configuration>.minus(other: Provider<Configuration>): FileCollection =
	this.get() - other.get()
