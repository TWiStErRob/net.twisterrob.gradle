plugins {
//	kotlin("jvm")
	`java-gradle-plugin`
}

base.archivesBaseName = "twister-quality"

val VERSION_ANDROID_PLUGIN: String by project
val VERSION_VIOLATIONS: String by project
val VERSION_XML_BUILDER: String by project

dependencies {
	implementation(project(":common"))
	implementation(project(":checkstyle"))
	implementation(project(":pmd"))

	compileOnly("com.android.tools.build:gradle:${VERSION_ANDROID_PLUGIN}")
//	compileOnly ("de.aaschmid:gradle-cpd-plugin:1.0")
	implementation("se.bjurr.violations:violations-lib:${VERSION_VIOLATIONS}")
	implementation("org.redundent:kotlin-xml-builder:${VERSION_XML_BUILDER}")

	testImplementation(project(":test"))
	testImplementation(project(":test:internal"))
	testRuntime("com.android.tools.build:gradle:${VERSION_ANDROID_PLUGIN}")
}

listOf(":test", ":checkstyle", ":pmd").forEach(project::pullTestResourcesFrom)
