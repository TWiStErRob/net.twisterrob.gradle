package net.twisterrob.gradle.root

import net.twisterrob.gradle.common.BaseExposedPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

@Suppress("detekt.UnnecessaryAbstractClass") // Gradle convention.
abstract class RootPlugin : BaseExposedPlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		project.apply<GradlePlugin>()
	}
}
