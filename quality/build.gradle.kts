plugins {
//	kotlin("jvm")
	`java-gradle-plugin`
}

base.archivesBaseName = "twister-quality"

val VERSION_ANDROID_PLUGIN: String by project
val VERSION_VIOLATIONS: String by project
val VERSION_JUNIT: String by project
val VERSION_HAMCREST: String by project
val VERSION_JETBRAINS_ANNOTATIONS: String by project

dependencies {
	implementation(project(":common"))
	implementation(project(":checkstyle"))
	implementation(project(":pmd"))

	compileOnly("com.android.tools.build:gradle:${VERSION_ANDROID_PLUGIN}")
//	compileOnly ("de.aaschmid:gradle-cpd-plugin:1.0")
	implementation("se.bjurr.violations:violations-lib:${VERSION_VIOLATIONS}")

	testImplementation(gradleTestKit())
	testImplementation(project(":test"))

	testImplementation("junit:junit:${VERSION_JUNIT}")
	testImplementation("org.hamcrest:hamcrest-all:${VERSION_HAMCREST}")
	testImplementation("org.jetbrains:annotations:${VERSION_JETBRAINS_ANNOTATIONS}")
}

listOf( ":test", ":checkstyle", ":pmd" ).forEach(project::pullTestResourcesFrom)
