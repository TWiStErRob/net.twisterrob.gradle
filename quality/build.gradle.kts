plugins {
//	kotlin("jvm")
	`java-gradle-plugin`
}

base.archivesBaseName = "twister-quality"

dependencies {
	implementation(project(":common"))
	implementation(project(":checkstyle"))
	implementation(project(":pmd"))

	compileOnly(Libs.Android.plugin)
//	compileOnly ("de.aaschmid:gradle-cpd-plugin:1.0")
	implementation(Libs.violations)

	testImplementation(project(":test"))
	testImplementation(project(":test:internal"))
	testRuntime(Libs.Android.plugin)
}

listOf(":test", ":checkstyle", ":pmd").forEach(project::pullTestResourcesFrom)
