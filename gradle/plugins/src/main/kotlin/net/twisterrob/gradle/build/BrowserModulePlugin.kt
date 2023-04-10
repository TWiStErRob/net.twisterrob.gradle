package net.twisterrob.gradle.build

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

class BrowserModulePlugin : Plugin<Project> {

	override fun apply(project: Project) {
		val sourcesOnly = project.configurations.create("sourcesOnly") {
			this.description = "Classpath to be exposed to IntelliJ IDEA for Gradle Sync."
			this.isVisible = true
			this.isCanBeResolved = true
			this.isCanBeConsumed = false
		}
		// TODO reduce this to just the bare minimum required by IDEA which is unknown at the moment.
		// It might be as simple as creating a configuration with the right name, or right attributes.
		project.plugins.apply("org.gradle.java")
		project.configurations.named(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME) {
			this.extendsFrom(sourcesOnly)
		}
	}
}
