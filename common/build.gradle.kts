plugins {
	id("net.twisterrob.gradle.build.module.library")
	id("net.twisterrob.gradle.build.publish")
	id("org.gradle.groovy")
}

base.archivesName.set("twister-quality-common")
description = "Common: Shared classes between checkers. (Not to be consumed directly.)"

dependencies {
	implementation(gradleApiWithoutKotlin())
	implementation(projects.compat.agpBase)
	implementation(projects.compat.gradle)

	compileOnly(libs.android.gradle)
	// com.android.SdkConstants.FD_GENERATED
	compileOnly(libs.android.tools.common)
	compileOnly(libs.annotations.jsr305)
	compileOnly(libs.annotations.jetbrains)

	testImplementation(projects.test.internal)
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
	classpath += files(kotlinTask.map { it.destinationDirectory })
}
