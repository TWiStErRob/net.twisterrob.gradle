plugins {
	`java`
	`groovy`
}
apply {
	plugin("kotlin")
}

base.archivesBaseName = "twister-quality-common"

val VERSION_ANDROID_PLUGIN by project
val VERSION_JSR305_ANNOTATIONS by project
val VERSION_JETBRAINS_ANNOTATIONS by project
val VERSION_JUNIT by project

dependencies {
	implementation(gradleApi())

	compileOnly("com.android.tools.build:gradle:${VERSION_ANDROID_PLUGIN}")
	compileOnly("com.google.code.findbugs:jsr305:${VERSION_JSR305_ANNOTATIONS}")

	testImplementation("org.jetbrains:annotations:${VERSION_JETBRAINS_ANNOTATIONS}")
	testImplementation("junit:junit:${VERSION_JUNIT}")
	testImplementation("com.google.guava:guava:22.0")
}

// don't double-compile Java classes
java.sourceSets.all { kotlin.srcDirs -= java.srcDirs }
// make TFO visible to Groovy
val groovyTask = tasks["compileTestGroovy"] as org.gradle.api.tasks.compile.GroovyCompile
val kotlinTask = tasks["compileTestKotlin"] as org.jetbrains.kotlin.gradle.tasks.KotlinCompile
groovyTask.dependsOn(kotlinTask)
groovyTask.classpath += files(kotlinTask.destinationDir)
