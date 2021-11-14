plugins {
//	kotlin("jvm")
	`groovy`
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-quality-common")
description = "Common: Shared classes between checkers. (Not to be consumed directly.)"

dependencies {
	implementation(gradleApiWithoutKotlin())

	compileOnly(Libs.Android.plugin)
	compileOnly(libs.jsr305)

	testImplementation(projects.test.internal)
	testImplementation(Libs.guava)
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
