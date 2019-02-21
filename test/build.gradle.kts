plugins {
//	kotlin("jvm")
	`java-gradle-plugin`
}

base.archivesBaseName = "twister-gradle-test"

val VERSION_JUNIT: String by project
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
	testImplementation(project(":test:internal"))
}

// Need to depend on the real artifact so TestPluginTest can work
val jar by tasks
tasks {
	"test" {
		dependsOn(jar)
		doFirst {
			val artifactPath = jar.outputs.files.singleFile.parentFile
			(this as Test).jvmArgs("-Dnet.twisterrob.gradle.test.artifactPath=${artifactPath}")
		}
	}
}

afterEvaluate {
	//noinspection UnnecessaryQualifiedReference keep it explicitly together with code
	val metaTask = tasks["pluginUnderTestMetadata"] as org.gradle.plugin.devel.tasks.PluginUnderTestMetadata
	metaTask.pluginClasspath = files(
		configurations.runtimeClasspath - configurations.compileOnly,
		jar.outputs.files.singleFile
	)
}
