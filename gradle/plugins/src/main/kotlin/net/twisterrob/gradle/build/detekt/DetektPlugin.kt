package net.twisterrob.gradle.build.detekt

import dev.detekt.gradle.Detekt
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
		project.plugins.apply("dev.detekt")
		project.detekt {
			// TODEL https://github.com/detekt/detekt/issues/4926
			buildUponDefaultConfig = false
			allRules = true
			ignoreFailures = isCI
			//debug = true
			config.from(project.rootProject.file("config/detekt/detekt.yml"))
			baseline = project.rootProject.file("config/detekt/detekt-baseline-${project.slug}.xml")
			basePath = project.rootProject.layout.projectDirectory

			parallel = true

			// Detekt 2 only accepts Kotlin 2.x language/API versions, unlike this project's 1.8 compatibility settings.
			val detektKotlinVersion = project.libs.versions.kotlin.build.map { it.substringBeforeLast(".") }
			project.tasks.withType<Detekt>().configureEach {
				apiVersion = detektKotlinVersion
				languageVersion = detektKotlinVersion
				jvmTarget = project.libs.versions.java.get()
				// Detekt falsely resolves this to DetektExtension.report because of Kotlin DSL.
				@Suppress("detekt.Deprecation")
				reports {
					html.required = true // human
					markdown.required = true // console
				}
			}
		}
	}
}
