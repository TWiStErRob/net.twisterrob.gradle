plugins {
//	kotlin("jvm")
	`java-gradle-plugin`
}

base.archivesBaseName = "twister-gradle-test"

val VERSION_JSR305_ANNOTATIONS: String by project
val VERSION_JETBRAINS_ANNOTATIONS: String by project

dependencies {
	compileOnly(gradleApi())
	compileOnly(gradleTestKit())

	implementation(project(":common"))
	compileOnly(Libs.JUnit4.library)
	compileOnly(Libs.JUnit5.api)
	compileOnly("com.google.code.findbugs:jsr305:${VERSION_JSR305_ANNOTATIONS}")
	compileOnly("org.jetbrains:annotations:${VERSION_JETBRAINS_ANNOTATIONS}")

	testImplementation(gradleApi())
	testImplementation(gradleTestKit())
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
