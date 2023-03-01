package net.twisterrob.gradle.build

import libs
import net.twisterrob.gradle.build.detekt.DetektRootPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class RootModulePlugin : Plugin<Project> {

	override fun apply(project: Project) {
		project.plugins.apply(DetektRootPlugin::class)

		val target = project.libs.versions.kotlin.target.get()
		val language = project.libs.versions.kotlin.language.get()
		check(target.startsWith(language)) {
			error("Kotlin target version ($target) must be compatible with language version ($language).")
		}
	}
}
