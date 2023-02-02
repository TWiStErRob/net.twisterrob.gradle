package net.twisterrob.gradle.build

import net.twisterrob.gradle.build.detekt.DetektRootPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import versionCatalogs

class RootModulePlugin : Plugin<Project> {

	override fun apply(project: Project) {
		project.plugins.apply(DetektRootPlugin::class)

		val libs = project.versionCatalogs.named("libs")
		val target = libs.findVersion("kotlin-target").get().requiredVersion
		val language = libs.findVersion("kotlin-language").get().requiredVersion
		check(target.startsWith(language)) {
			error("Kotlin target version ($target) must be compatible with language version ($language).")
		}
	}
}
