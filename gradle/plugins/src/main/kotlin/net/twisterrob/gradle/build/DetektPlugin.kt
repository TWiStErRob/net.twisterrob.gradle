package net.twisterrob.gradle.build

import io.gitlab.arturbosch.detekt.Detekt
import isCI
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import slug

internal class DetektPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		project.plugins.apply("io.gitlab.arturbosch.detekt")
		project.detekt {
			// TODEL https://github.com/detekt/detekt/issues/4926
			buildUponDefaultConfig = false
			allRules = true
			ignoreFailures = isCI
			//debug = true
			config = project.rootProject.files("config/detekt/detekt.yml")
			baseline = project.rootProject.file("config/detekt/detekt-baseline-${project.slug}.xml")
			basePath = project.rootProject.projectDir.absolutePath

			parallel = true

			project.tasks.withType<Detekt>().configureEach {
				reports {
					html.required.set(true) // human
					txt.required.set(true) // console
				}
			}
		}
	}
}
