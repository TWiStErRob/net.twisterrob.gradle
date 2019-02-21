plugins {
//	kotlin("jvm")
	`java-gradle-plugin`
}

base.archivesBaseName = "twister-quality-checkstyle"

val VERSION_ANDROID_PLUGIN: String by project

dependencies {
	implementation(project(":common"))

	compileOnly("com.android.tools.build:gradle:${VERSION_ANDROID_PLUGIN}")

	testImplementation(project(":test"))
	testImplementation(project(":test:internal"))
}

pullTestResourcesFrom(":test")
