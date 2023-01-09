package net.twisterrob.gradle.build.detekt

import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

internal fun Project.detekt(block: DetektExtension.() -> Unit) {
	project.extensions.configure(block)
}
