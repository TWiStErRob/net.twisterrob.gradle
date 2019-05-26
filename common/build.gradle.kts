plugins {
//	kotlin("jvm")
	`groovy`
}

base.archivesBaseName = "twister-quality-common"

val VERSION_ANDROID_PLUGIN: String by project
val VERSION_JSR305_ANNOTATIONS: String by project

dependencies {
	implementation(gradleApi())

	compileOnly("com.android.tools.build:gradle:${VERSION_ANDROID_PLUGIN}")
	compileOnly("com.google.code.findbugs:jsr305:${VERSION_JSR305_ANNOTATIONS}")

	testImplementation(project(":test:internal"))
	testImplementation("com.google.guava:guava:22.0")
}

tasks.withType<JavaCompile> {
	options.compilerArgs as MutableCollection<String> += listOf(
		"-proc:none" // disable annotation processing (not used, hides auto-value processors being on classpath)
	)
}

// don't double-compile Java classes
java.sourceSets.all { kotlin.srcDirs -= java.srcDirs }

// make TFO visible to Groovy
val groovyTask = tasks["compileTestGroovy"] as org.gradle.api.tasks.compile.GroovyCompile
val kotlinTask = tasks["compileTestKotlin"] as org.jetbrains.kotlin.gradle.tasks.KotlinCompile
groovyTask.dependsOn(kotlinTask)
groovyTask.classpath += files(kotlinTask.destinationDir)
