plugins {
//	kotlin("jvm")
	`java-gradle-plugin`
}

base.archivesBaseName = "twister-quality-checkstyle"

dependencies {
	implementation(project(":common"))

	compileOnly(Libs.Android.plugin)

	testImplementation(project(":test"))
	testImplementation(project(":test:internal"))
}

pullTestResourcesFrom(":test")
