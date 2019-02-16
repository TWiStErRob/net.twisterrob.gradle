plugins {
//	kotlin("jvm")
	`java-gradle-plugin`
}

base.archivesBaseName = "twister-gradle-test"

val VERSION_JUNIT: String by project
val VERSION_JUNIT_JUPITER: String by project
val VERSION_MOCKITO: String by project
val VERSION_MOCKITO_KOTLIN: String by project
val VERSION_HAMCREST: String by project
val VERSION_JSR305_ANNOTATIONS: String by project
val VERSION_JETBRAINS_ANNOTATIONS: String by project
val VERSION_LINT: String by project

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
	testImplementation("org.junit.jupiter:junit-jupiter-api:$VERSION_JUNIT_JUPITER")
	testImplementation("org.junit.jupiter:junit-jupiter-migrationsupport:$VERSION_JUNIT_JUPITER")
	testImplementation("org.junit.jupiter:junit-jupiter-params:$VERSION_JUNIT_JUPITER")
	testRuntime("org.junit.jupiter:junit-jupiter-engine:$VERSION_JUNIT_JUPITER")
	testRuntime("org.junit.vintage:junit-vintage-engine:$VERSION_JUNIT_JUPITER")
	testImplementation("org.hamcrest:java-hamcrest:${VERSION_HAMCREST}")
	testImplementation("org.mockito:mockito-core:${VERSION_MOCKITO}")
	testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:${VERSION_MOCKITO_KOTLIN}")
	testImplementation("org.jetbrains:annotations:${VERSION_JETBRAINS_ANNOTATIONS}")
	// only here so IDEA can browse the source files of this dependency when getting a stack trace or finding usages
	testRuntimeOnly("com.android.tools.lint:lint:${VERSION_LINT}") { isTransitive = false }
	testRuntimeOnly("com.android.tools.lint:lint-api:${VERSION_LINT}") { isTransitive = false }
	testRuntimeOnly("com.android.tools.lint:lint-gradle:${VERSION_LINT}") { isTransitive = false }
	testRuntimeOnly("com.android.tools.lint:lint-gradle-api:${VERSION_LINT}") { isTransitive = false }
	testRuntimeOnly("com.android.tools.lint:lint-checks:${VERSION_LINT}") { isTransitive = false }
	testRuntimeOnly("com.android.tools.lint:lint-kotlin:${VERSION_LINT}") { isTransitive = false }
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

tasks.withType<Test> { useJUnitPlatform() }

afterEvaluate {
	//noinspection UnnecessaryQualifiedReference keep it explicitly together with code
	val metaTask = tasks["pluginUnderTestMetadata"] as org.gradle.plugin.devel.tasks.PluginUnderTestMetadata
	metaTask.pluginClasspath = files(
			configurations.runtimeClasspath - configurations.compileOnly,
			jar.outputs.files.singleFile
	)
}
