package net.twisterrob.gradle.build;

import org.gradle.api.Plugin
import org.gradle.api.Project

class BrowserModulePlugin : Plugin<Project> {

	override fun apply(project: Project) {
		project.plugins.apply("org.gradle.java")
	}
}
