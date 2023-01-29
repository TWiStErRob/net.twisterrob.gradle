package net.twisterrob.gradle.build;

import net.twisterrob.gradle.build.compilation.JavaCompatibilityPlugin
import net.twisterrob.gradle.build.detekt.DetektPlugin
import net.twisterrob.gradle.build.testing.InitScriptMetadataPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class GradlePluginModulePlugin : Plugin<Project> {

	override fun apply(project: Project) {
		project.plugins.apply("org.gradle.java-gradle-plugin")
		project.plugins.apply(InitScriptMetadataPlugin::class)

		project.plugins.apply("org.jetbrains.kotlin.jvm")
		project.plugins.apply(JavaCompatibilityPlugin::class)
		//project.plugins.apply("org.gradle.java-test-fixtures")

		project.plugins.apply(DetektPlugin::class)
	}
}
