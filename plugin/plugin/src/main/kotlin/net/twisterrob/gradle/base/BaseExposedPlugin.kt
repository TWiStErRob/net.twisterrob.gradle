package net.twisterrob.gradle.base

import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

open class BaseExposedPlugin : BasePlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		project.apply<GradlePlugin>()
	}
}
