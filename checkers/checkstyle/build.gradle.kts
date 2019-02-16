plugins {
//	kotlin("jvm")
	`java-gradle-plugin`
}

base.archivesBaseName = "twister-quality-checkstyle"

val VERSION_ANDROID_PLUGIN: String by project
val VERSION_JUNIT: String by project
val VERSION_HAMCREST: String by project
val VERSION_JETBRAINS_ANNOTATIONS: String by project

dependencies {
	implementation(project(":common"))

	compileOnly("com.android.tools.build:gradle:${VERSION_ANDROID_PLUGIN}")

	testImplementation(project(":test"))
	testImplementation("junit:junit:${VERSION_JUNIT}")
	testImplementation("org.hamcrest:java-hamcrest:${VERSION_HAMCREST}")
	testImplementation("org.jetbrains:annotations:${VERSION_JETBRAINS_ANNOTATIONS}")
}

pullTestResourcesFrom(":test")
