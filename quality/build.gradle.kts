plugins {
//	kotlin("jvm")
	`java-gradle-plugin`
}

base.archivesBaseName = "twister-quality"

val VERSION_VIOLATIONS: String by project

dependencies {
	implementation(project(":common"))
	implementation(project(":checkstyle"))
	implementation(project(":pmd"))

	compileOnly(Libs.Android.plugin)
//	compileOnly ("de.aaschmid:gradle-cpd-plugin:1.0")
	implementation("se.bjurr.violations:violations-lib:${VERSION_VIOLATIONS}")

	testImplementation(project(":test"))
	testImplementation(project(":test:internal"))
	testRuntime(Libs.Android.plugin)
}

listOf(":test", ":checkstyle", ":pmd").forEach(project::pullTestResourcesFrom)
