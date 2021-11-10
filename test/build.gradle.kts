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

	compileOnly(Libs.JUnit4.library)
	compileOnly(Libs.JUnit5.api)
	compileOnly(Libs.Annotations.jsr305)
	compileOnly(Libs.Annotations.jetbrains)

	testImplementation(gradleApiWithoutKotlin())
	testImplementation(gradleTestKitWithoutKotlin())
	testImplementation(project(":test:internal"))
}

// Need to depend on the real artifact so TestPluginTest can work
tasks.named<Test>("test") {
	dependsOn("jar")
	doFirst {
		val jarArtifactPath = tasks.jar.get().outputs.files.singleFile.parentFile
		(this as Test).jvmArgs("-Dnet.twisterrob.gradle.test.artifactPath=${jarArtifactPath}")
	}
}

tasks.named<PluginUnderTestMetadata>("pluginUnderTestMetadata") {
	val jarArtifact = tasks.jar.get().outputs.files.singleFile
	pluginClasspath.apply {
		setFrom()
		from(configurations.runtimeClasspath - configurations.compileOnly)
		from(jarArtifact)
	}
}

inline val TaskContainer.jar get() = named<Jar>("jar")

// Polyfill for Gradle 5
operator fun Provider<Configuration>.minus(other: Provider<Configuration>) =
	this.get() - other.get()
