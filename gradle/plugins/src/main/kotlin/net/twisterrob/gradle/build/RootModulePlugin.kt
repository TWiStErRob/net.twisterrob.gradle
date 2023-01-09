package net.twisterrob.gradle.build

import net.twisterrob.gradle.build.detekt.DetektRootPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class RootModulePlugin : Plugin<Project> {

	override fun apply(project: Project) {
		project.plugins.apply(DetektRootPlugin::class)
	}
}
