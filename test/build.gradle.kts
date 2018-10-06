plugins {
//	kotlin("jvm")
	`java-gradle-plugin`
}

base.archivesBaseName = "twister-gradle-test"

val VERSION_JUNIT: String by project
val VERSION_MOCKITO: String by project
val VERSION_HAMCREST: String by project
val VERSION_JSR305_ANNOTATIONS: String by project
val VERSION_JETBRAINS_ANNOTATIONS: String by project

dependencies {
	compileOnly(gradleApi())
	compileOnly(gradleTestKit())

	implementation(project(":common"))
	compileOnly("junit:junit:${VERSION_JUNIT}")
	compileOnly("com.google.code.findbugs:jsr305:${VERSION_JSR305_ANNOTATIONS}")
	compileOnly("org.jetbrains:annotations:${VERSION_JETBRAINS_ANNOTATIONS}")

	testImplementation(gradleApi())
	testImplementation(gradleTestKit())
	testImplementation("junit:junit:${VERSION_JUNIT}")
	testImplementation("org.hamcrest:hamcrest-all:${VERSION_HAMCREST}")
	testImplementation("org.mockito:mockito-core:${VERSION_MOCKITO}")
	testImplementation("org.jetbrains:annotations:${VERSION_JETBRAINS_ANNOTATIONS}")
}

// Need to depend on the real artifact so TestPluginTest can work
tasks {
	named<Test>("test") {
		dependsOn("jar")
		doFirst {
			val jar by tasks.named<Jar>("jar")
			val artifactPath = jar.outputs.files.singleFile.parentFile
			(this as Test).jvmArgs("-Dnet.twisterrob.gradle.test.artifactPath=${artifactPath}")
		}
	}
}
//noinspection UnnecessaryQualifiedReference keep it explicitly together with code
tasks.named<org.gradle.plugin.devel.tasks.PluginUnderTestMetadata>("pluginUnderTestMetadata") {
	pluginClasspath.apply{
		setFrom()
		from(configurations.runtimeClasspath - configurations.compileOnly)
		from(tasks.getByName<Jar>("jar").outputs.files.singleFile)
	}
}
