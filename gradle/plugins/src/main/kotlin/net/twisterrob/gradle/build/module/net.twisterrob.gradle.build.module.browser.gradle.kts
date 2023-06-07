import net.twisterrob.gradle.build.compilation.JavaCompatibilityPlugin

// TODO reduce this to just the bare minimum required by IDEA which is unknown at the moment.
// It might be as simple as creating a configuration with the right name, or right attributes.

plugins {
	id("org.gradle.java")
}
plugins.apply(JavaCompatibilityPlugin::class)

val sourcesOnly: Configuration by configurations.creating {
	description = "Classpath to be exposed to IntelliJ IDEA for Gradle Sync."
	isVisible = true
	isCanBeResolved = true
	isCanBeConsumed = false
}

configurations.named(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME) {
	extendsFrom(sourcesOnly)
}
