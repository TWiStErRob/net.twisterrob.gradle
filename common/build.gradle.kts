plugins {
//	kotlin("jvm")
	`groovy`
	id("net.twisterrob.gradle.build.publishing")
}

base.archivesName.set("twister-quality-common")
description = "Common: Shared classes between checkers. (Not to be consumed directly.)"

dependencies {
	implementation(gradleApiWithoutKotlin())
	implementation(projects.compat.agp)
	implementation(projects.compat.gradle)

	compileOnly(libs.android.gradle)
	compileOnly(libs.annotations.jsr305)
	compileOnly(libs.annotations.jetbrains)

	testImplementation(projects.test.internal)
	testImplementation(libs.guava)
	testRuntimeOnly(libs.android.gradle)
}

tasks.withType<JavaCompile>().configureEach {
	options.compilerArgs as MutableCollection<String> += listOf(
		"-proc:none" // disable annotation processing (not used, hides auto-value processors being on classpath)
	)
}

// don't double-compile Java classes
java.sourceSets.all { kotlin.srcDirs -= java.srcDirs }

// make TFO visible to Groovy
tasks.named<GroovyCompile>("compileTestGroovy") {
	val kotlinTask = tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileTestKotlin")
	dependsOn(kotlinTask)
	classpath += files(kotlinTask.get().destinationDir)
}
