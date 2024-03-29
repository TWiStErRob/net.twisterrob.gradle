package net.twisterrob.gradle.build.detekt

import io.gitlab.arturbosch.detekt.Detekt
import net.twisterrob.gradle.build.dsl.detekt
import net.twisterrob.gradle.build.dsl.isCI
import net.twisterrob.gradle.build.dsl.libs
import net.twisterrob.gradle.slug
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.withType

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
internal abstract class DetektPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		project.plugins.apply("io.gitlab.arturbosch.detekt")
		project.detekt {
			// TODEL https://github.com/detekt/detekt/issues/4926
			buildUponDefaultConfig = false
			allRules = true
			ignoreFailures = isCI
			//debug = true
			config.setFrom(project.rootProject.file("config/detekt/detekt.yml"))
			baseline = project.rootProject.file("config/detekt/detekt-baseline-${project.slug}.xml")
			basePath = project.rootProject.projectDir.absolutePath

			parallel = true

			project.tasks.withType<Detekt>().configureEach {
				@Suppress("detekt.MaxChainedCallsOnSameLine")
				languageVersion = project.libs.versions.kotlin.language.get()
				jvmTarget = project.libs.versions.java.get()
				// Detekt falsely resolves this to DetektExtension.report because of Kotlin DSL.
				@Suppress("detekt.Deprecation")
				reports {
					html.required = true // human
					txt.required = true // console
				}
			}
		}
	}
}
