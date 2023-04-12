package net.twisterrob.gradle.build.dsl

import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByName

internal val Project.detekt: DetektExtension
	get() = this.extensions.getByName<DetektExtension>("detekt")

internal fun Project.detekt(block: DetektExtension.() -> Unit) {
	detekt.block()
}
