plugins {
//	kotlin("jvm")
	`groovy`
}

base.archivesBaseName = "twister-quality-common"

dependencies {
	implementation(gradleApi())

	compileOnly(Libs.Android.plugin)
	compileOnly(Libs.Annotations.jsr305)

	testImplementation(project(":test:internal"))
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
