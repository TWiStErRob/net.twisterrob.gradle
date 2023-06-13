import net.twisterrob.gradle.webjars.ExtractWebJarsTask

plugins {
	id("org.gradle.jvm-ecosystem")
}

val webjars by configurations.registering {
	attributes {
		attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
	}
	isVisible = false
	isCanBeConsumed = false
	isCanBeResolved = true
}

// Expose it to the compiler, so it's visible as a dependency too.
configurations.named("compileOnly").configure { extendsFrom(webjars.get()) }

intoFirstResourceFolder()

fun intoFirstResourceFolder() {
	val extractWebJars by tasks.registering(ExtractWebJarsTask::class) {
		fromConfiguration(configurations.named("webjars"))
		outputDirectory.set(project.layout.dir(sourceSets.named("main").map { it.resources.srcDirs.first() }))
	}
	tasks.named("processResources").configure { dependsOn(extractWebJars) }
}

// Note: alternative config creating another resource folder. It works, but doesn't allow local testing.
fun asSeparateResourceFolder() {
	val extractWebJars by tasks.registering(ExtractWebJarsTask::class) {
		fromConfiguration(configurations.named("webjars"))
		cleanFirst.set(true)
		outputDirectory.set(project.layout.buildDirectory.dir("webjars-as-resources"))
	}
	sourceSets.named("main").configure { resources.srcDir(extractWebJars) }
}
