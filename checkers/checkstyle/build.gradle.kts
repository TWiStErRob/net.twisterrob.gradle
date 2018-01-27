plugins {
//	kotlin("jvm")
	`java-gradle-plugin`
}

base.archivesBaseName = "twister-quality-checkstyle"

val VERSION_ANDROID_PLUGIN by project
val VERSION_JUNIT by project
val VERSION_HAMCREST by project
val VERSION_JETBRAINS_ANNOTATIONS by project

dependencies {
	implementation(project(":common"))

	compileOnly("com.android.tools.build:gradle:${VERSION_ANDROID_PLUGIN}")

	testImplementation(project(":test"))
	testImplementation("junit:junit:${VERSION_JUNIT}")
	testImplementation("org.hamcrest:hamcrest-all:${VERSION_HAMCREST}")
	testImplementation("org.jetbrains:annotations:${VERSION_JETBRAINS_ANNOTATIONS}")
}

pullTestResourcesFrom(":test")
