import net.twisterrob.gradle.webjars.ExtractWebJarsExtension
import net.twisterrob.gradle.webjars.ExtractWebJarsTask

extensions.create<ExtractWebJarsExtension>("webjars")

val webjars by configurations.registering {
	isVisible = false
	isCanBeConsumed = false
	isCanBeResolved = true
}

// Expose it to the compiler, so it's visible as a dependency too.
configurations.named("compileOnly").configure { extendsFrom(webjars.get()) }

val extractWebJars by tasks.registering(ExtractWebJarsTask::class) {
	fromConfiguration(configurations.named("webjars"))
	cleanFirst = true
	outputDirectory = project.layout.buildDirectory.dir("webjars-as-resources")
}
