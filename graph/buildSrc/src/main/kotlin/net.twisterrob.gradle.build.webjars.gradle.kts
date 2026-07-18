import net.twisterrob.gradle.webjars.ExtractWebJarsExtension
import net.twisterrob.gradle.webjars.ExtractWebJarsTask

extensions.create<ExtractWebJarsExtension>("webjars")

val webjars = configurations.register("webjars") {
	isCanBeConsumed = false
	isCanBeResolved = true
}

// Expose it to the compiler, so it's visible as a dependency too.
configurations.named("compileOnly").configure { extendsFrom(webjars.get()) }

val extractWebJars = tasks.register<ExtractWebJarsTask>("extractWebJars") {
	fromConfiguration(configurations.named("webjars"))
	cleanFirst = true
	outputDirectory = project.layout.buildDirectory.dir("webjars-as-resources")
}
